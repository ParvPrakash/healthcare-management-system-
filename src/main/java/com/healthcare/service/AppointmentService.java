package com.healthcare.service;

import com.healthcare.concurrent.AsyncNotificationManager;
import com.healthcare.concurrent.MetricsTracker;
import com.healthcare.concurrent.SlotLockManager;
import com.healthcare.dao.*;
import com.healthcare.exception.AppointmentConflictException;
import com.healthcare.exception.DatabaseException;
import com.healthcare.exception.ResourceNotFoundException;
import com.healthcare.exception.ValidationException;
import com.healthcare.model.*;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service managing appointment lifecycles, transactional scheduling,
 * and high-concurrency double-booking collision prevention.
 */
public class AppointmentService {
    private static final Logger logger = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentDao appointmentDao;
    private final ScheduleDao scheduleDao;
    private final NotificationDao notificationDao;
    private final AuditDao auditDao;

    public AppointmentService() {
        this.appointmentDao = new AppointmentDaoImpl();
        this.scheduleDao = new ScheduleDaoImpl();
        this.notificationDao = new NotificationDaoImpl();
        this.auditDao = new AuditDaoImpl();
    }

    public AppointmentService(AppointmentDao appointmentDao, ScheduleDao scheduleDao,
                              NotificationDao notificationDao, AuditDao auditDao) {
        this.appointmentDao = appointmentDao;
        this.scheduleDao = scheduleDao;
        this.notificationDao = notificationDao;
        this.auditDao = auditDao;
    }

    /**
     * Books an appointment with double-booking prevention.
     * Combines ReentrantLock (§4.3) + TRANSACTION_REPEATABLE_READ + SELECT FOR UPDATE (§4.6).
     */
    public Appointment bookAppointment(int patientId, int doctorId, int slotId, String reason, String ipAddress)
            throws AppointmentConflictException, ValidationException, ResourceNotFoundException, DatabaseException {

        if (reason == null || reason.trim().isEmpty()) {
            throw new ValidationException("Consultation reason is required");
        }

        // 1. Acquire fine-grained lock for this slot (concurrency guard)
        boolean lockAcquired = SlotLockManager.tryLock(slotId, 3, TimeUnit.SECONDS);
        if (!lockAcquired) {
            logger.warn("Concurrency collision: could not acquire slot lock for slot {}", slotId);
            throw new AppointmentConflictException("This time slot is currently being processed by another user. Please choose another slot.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            // 2. Begin strict database transaction
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            try {
                // 3. Row lock the slot row with SELECT ... FOR UPDATE
                Optional<TimeSlot> slotOpt = scheduleDao.findSlotByIdForUpdate(conn, slotId);
                if (slotOpt.isEmpty()) {
                    throw new ResourceNotFoundException("Selected time slot no longer exists");
                }

                TimeSlot slot = slotOpt.get();
                if (slot.isBooked()) {
                    throw new AppointmentConflictException("This time slot has already been booked by another patient.");
                }

                if (!Objects.equals(slot.getDoctorId(), doctorId)) {
                    throw new ValidationException("Selected slot does not belong to the requested doctor");
                }

                // 4. Mark slot as booked
                scheduleDao.markSlotBooked(conn, slotId, true);

                // 5. Create appointment entity
                Appointment app = new Appointment();
                app.setDoctorId(doctorId);
                app.setPatientId(patientId);
                app.setSlotId(slotId);
                app.setAppointmentDate(slot.getSlotDate());
                app.setStartTime(slot.getStartTime());
                app.setEndTime(slot.getEndTime());
                app.setStatus(AppointmentStatus.CONFIRMED);
                app.setReason(reason.trim());
                app = appointmentDao.saveTransactional(conn, app);

                // 6. Generate in-app notifications
                Notification notifPatient = new Notification(patientId, "Appointment Confirmed",
                        "Your appointment for " + slot.getSlotDate() + " at " + slot.getStartTime() + " is confirmed.", "SUCCESS");
                notificationDao.saveTransactional(conn, notifPatient);

                Notification notifDoc = new Notification(doctorId, "New Appointment Booked",
                        "New appointment booked by patient #" + patientId + " on " + slot.getSlotDate() + " at " + slot.getStartTime(), "INFO");
                notificationDao.saveTransactional(conn, notifDoc);

                // 7. Audit log in transaction
                auditDao.logAction(conn, patientId, "BOOK_APPOINTMENT",
                        "Booked appointment #" + app.getId() + " with doctor #" + doctorId + " for slot #" + slotId, ipAddress);

                // 8. Commit transaction
                conn.commit();
                logger.info("Successfully committed booking transaction for appointment #{}", app.getId());

                // 9. Update live atomic metric counters
                MetricsTracker.incrementBookingsToday();

                // 10. Trigger asynchronous email/SMS dispatch via ExecutorService (§4.3)
                final Appointment bookedApp = app;
                AsyncNotificationManager.submit(() -> {
                    logger.info("[ASYNC DISPATCH] Sent confirmation email and SMS for appointment #{} to patient #{}",
                            bookedApp.getId(), patientId);
                });

                return app;

            } catch (SQLException | AppointmentConflictException | ResourceNotFoundException | ValidationException e) {
                conn.rollback();
                logger.warn("Transaction rolled back for slot booking {}: {}", slotId, e.getMessage());
                if (e instanceof AppointmentConflictException ace) throw ace;
                if (e instanceof ResourceNotFoundException rnfe) throw rnfe;
                if (e instanceof ValidationException ve) throw ve;
                throw new DatabaseException("Transaction error while booking appointment", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error acquiring connection for booking", e);
        } finally {
            // Always release lock
            SlotLockManager.unlock(slotId);
        }
    }

    /**
     * Reschedules an existing appointment to a new slot.
     * Uses ACID transaction across old slot, new slot, and appointment record (§4.6).
     */
    public boolean rescheduleAppointment(int appointmentId, int newSlotId, Integer requestingUserId, String ipAddress)
            throws AppointmentConflictException, ResourceNotFoundException, DatabaseException {

        boolean lockAcquired = SlotLockManager.tryLock(newSlotId, 3, TimeUnit.SECONDS);
        if (!lockAcquired) {
            throw new AppointmentConflictException("Target time slot is currently being processed. Please choose another.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            try {
                Appointment currentApp = appointmentDao.findById(appointmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

                // Check and lock new slot
                Optional<TimeSlot> newSlotOpt = scheduleDao.findSlotByIdForUpdate(conn, newSlotId);
                if (newSlotOpt.isEmpty() || newSlotOpt.get().isBooked()) {
                    throw new AppointmentConflictException("The selected new slot is unavailable");
                }
                TimeSlot newSlot = newSlotOpt.get();

                // 1. Release old slot
                scheduleDao.markSlotBooked(conn, currentApp.getSlotId(), false);

                // 2. Book new slot
                scheduleDao.markSlotBooked(conn, newSlotId, true);

                // 3. Update appointment
                appointmentDao.rescheduleTransactional(conn, appointmentId, newSlotId,
                        newSlot.getSlotDate(), newSlot.getStartTime(), newSlot.getEndTime());

                // 4. Notifications
                Notification notif = new Notification(currentApp.getPatientId(), "Appointment Rescheduled",
                        "Your appointment has been moved to " + newSlot.getSlotDate() + " at " + newSlot.getStartTime(), "INFO");
                notificationDao.saveTransactional(conn, notif);

                // 5. Audit
                auditDao.logAction(conn, requestingUserId, "RESCHEDULE_APPOINTMENT",
                        "Rescheduled appointment #" + appointmentId + " to slot #" + newSlotId, ipAddress);

                conn.commit();
                return true;

            } catch (SQLException | AppointmentConflictException | ResourceNotFoundException e) {
                conn.rollback();
                if (e instanceof AppointmentConflictException ace) throw ace;
                if (e instanceof ResourceNotFoundException rnfe) throw rnfe;
                throw new DatabaseException("Transaction rollback while rescheduling", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during reschedule", e);
        } finally {
            SlotLockManager.unlock(newSlotId);
        }
    }

    /**
     * Cancels an appointment and frees up its time slot.
     */
    public boolean cancelAppointment(int appointmentId, String cancelReason, Integer requestingUserId, String ipAddress)
            throws ResourceNotFoundException, DatabaseException {

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Appointment app = appointmentDao.findById(appointmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

                // 1. Free up slot
                scheduleDao.markSlotBooked(conn, app.getSlotId(), false);

                // 2. Update status
                appointmentDao.updateStatusTransactional(conn, appointmentId, AppointmentStatus.CANCELLED);

                // 3. Notify patient and doctor
                Notification notifPatient = new Notification(app.getPatientId(), "Appointment Cancelled",
                        "Appointment on " + app.getAppointmentDate() + " was cancelled: " + cancelReason, "DANGER");
                notificationDao.saveTransactional(conn, notifPatient);

                Notification notifDoc = new Notification(app.getDoctorId(), "Appointment Cancelled",
                        "Patient appointment on " + app.getAppointmentDate() + " was cancelled.", "WARNING");
                notificationDao.saveTransactional(conn, notifDoc);

                // 4. Audit
                auditDao.logAction(conn, requestingUserId, "CANCEL_APPOINTMENT",
                        "Cancelled appointment #" + appointmentId + " (Reason: " + cancelReason + ")", ipAddress);

                conn.commit();
                return true;
            } catch (SQLException | ResourceNotFoundException e) {
                conn.rollback();
                if (e instanceof ResourceNotFoundException rnfe) throw rnfe;
                throw new DatabaseException("Transaction rollback while cancelling", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error during cancel", e);
        }
    }

    public boolean completeAppointment(int appointmentId, Integer doctorId, String ipAddress)
            throws ResourceNotFoundException, DatabaseException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Appointment app = appointmentDao.findById(appointmentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

                appointmentDao.updateStatusTransactional(conn, appointmentId, AppointmentStatus.COMPLETED);

                Notification notif = new Notification(app.getPatientId(), "Appointment Completed",
                        "Your consultation with Dr. " + app.getDoctorName() + " is completed. Please leave your feedback!", "INFO");
                notificationDao.saveTransactional(conn, notif);

                auditDao.logAction(conn, doctorId, "COMPLETE_APPOINTMENT", "Completed appointment #" + appointmentId, ipAddress);

                conn.commit();
                return true;
            } catch (SQLException | ResourceNotFoundException e) {
                conn.rollback();
                if (e instanceof ResourceNotFoundException rnfe) throw rnfe;
                throw new DatabaseException("Error completing appointment", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Database error during completion", e);
        }
    }

    /**
     * Demonstrates Collections & Streams API: Groups appointments by Date (§4.2).
     */
    public Map<LocalDate, List<Appointment>> groupAppointmentsByDate(List<Appointment> list) {
        if (list == null) return Collections.emptyMap();
        return list.stream()
                .collect(Collectors.groupingBy(Appointment::getAppointmentDate, TreeMap::new, Collectors.toList()));
    }

    /**
     * Demonstrates Collections & Streams API: Counts appointments by Status (§4.2).
     */
    public Map<AppointmentStatus, Long> countAppointmentsByStatus(List<Appointment> list) {
        if (list == null) return Collections.emptyMap();
        return list.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
    }

    public Optional<Appointment> getAppointmentById(int id) throws DatabaseException {
        return appointmentDao.findById(id);
    }

    public List<Appointment> getPatientAppointments(int patientId, AppointmentStatus status) throws DatabaseException {
        return appointmentDao.searchAppointments(patientId, status);
    }

    public List<Appointment> getDoctorUpcomingAppointments(int doctorId, int limit) throws DatabaseException {
        return appointmentDao.findUpcomingByDoctor(doctorId, limit);
    }

    public List<Appointment> getDoctorPastAppointments(int doctorId, int limit) throws DatabaseException {
        return appointmentDao.findPastByDoctor(doctorId, limit);
    }

    public List<Appointment> searchAllAppointments(String query, AppointmentStatus status, int page, int pageSize) throws DatabaseException {
        int offset = Math.max(0, (page - 1) * pageSize);
        return appointmentDao.searchAppointments(query, status, pageSize, offset);
    }

    public List<Appointment> getTodayAppointments() throws DatabaseException {
        return appointmentDao.findTodayAppointments();
    }
}
