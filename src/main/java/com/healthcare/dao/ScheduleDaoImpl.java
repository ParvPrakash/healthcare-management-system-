package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Schedule;
import com.healthcare.model.TimeSlot;
import com.healthcare.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public class ScheduleDaoImpl implements ScheduleDao {
    private static final Logger logger = LoggerFactory.getLogger(ScheduleDaoImpl.class);

    private final RowMapper<Schedule> scheduleRowMapper = rs -> {
        Schedule s = new Schedule();
        s.setId(rs.getInt("id"));
        s.setDoctorId(rs.getInt("doctor_id"));
        s.setDayOfWeek(rs.getString("day_of_week"));
        s.setStartTime(rs.getTime("start_time").toLocalTime());
        s.setEndTime(rs.getTime("end_time").toLocalTime());
        s.setSlotDurationMinutes(rs.getInt("slot_duration_minutes"));
        s.setActive(rs.getBoolean("is_active"));
        return s;
    };

    private final RowMapper<TimeSlot> slotRowMapper = rs -> {
        TimeSlot ts = new TimeSlot();
        ts.setId(rs.getInt("id"));
        ts.setDoctorId(rs.getInt("doctor_id"));
        ts.setSlotDate(rs.getDate("slot_date").toLocalDate());
        ts.setStartTime(rs.getTime("start_time").toLocalTime());
        ts.setEndTime(rs.getTime("end_time").toLocalTime());
        ts.setBooked(rs.getBoolean("is_booked"));
        ts.setVersion(rs.getInt("version"));
        return ts;
    };

    @Override
    public Optional<Schedule> findById(Integer id) throws DatabaseException {
        String sql = "SELECT * FROM schedules WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(scheduleRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding schedule by id {}", id, e);
            throw new DatabaseException("Database error finding schedule", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Schedule> findAll() throws DatabaseException {
        String sql = "SELECT * FROM schedules ORDER BY doctor_id, day_of_week";
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(scheduleRowMapper.mapRow(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all schedules", e);
            throw new DatabaseException("Database error finding schedules", e);
        }
        return list;
    }

    @Override
    public List<Schedule> findByDoctorId(int doctorId) throws DatabaseException {
        String sql = "SELECT * FROM schedules WHERE doctor_id = ? AND is_active = TRUE ORDER BY day_of_week, start_time";
        List<Schedule> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(scheduleRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding schedules for doctor {}", doctorId, e);
            throw new DatabaseException("Database error finding doctor schedules", e);
        }
        return list;
    }

    @Override
    public void deleteByDoctorId(int doctorId) throws DatabaseException {
        String sql = "DELETE FROM schedules WHERE doctor_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error deleting schedules for doctor {}", doctorId, e);
            throw new DatabaseException("Database error deleting doctor schedules", e);
        }
    }

    @Override
    public List<TimeSlot> findSlotsByDoctorAndDate(int doctorId, LocalDate date) throws DatabaseException {
        String sql = "SELECT ts.*, u.name AS doctor_name, d.specialization AS doctor_spec " +
                "FROM time_slots ts " +
                "JOIN users u ON ts.doctor_id = u.id " +
                "JOIN doctors d ON ts.doctor_id = d.id " +
                "WHERE ts.doctor_id = ? AND ts.slot_date = ? " +
                "ORDER BY ts.start_time ASC";

        // Demonstrate Set/TreeSet for unique sorted slots
        TreeSet<TimeSlot> sortedSlots = new TreeSet<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, doctorId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TimeSlot slot = slotRowMapper.mapRow(rs);
                    slot.setDoctorName(rs.getString("doctor_name"));
                    slot.setDoctorSpecialization(rs.getString("doctor_spec"));
                    sortedSlots.add(slot);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding slots for doctor {} on date {}", doctorId, date, e);
            throw new DatabaseException("Database error retrieving doctor slots", e);
        }
        return new ArrayList<>(sortedSlots);
    }

    @Override
    public Optional<TimeSlot> findSlotById(int slotId) throws DatabaseException {
        String sql = "SELECT ts.*, u.name AS doctor_name, d.specialization AS doctor_spec " +
                "FROM time_slots ts " +
                "JOIN users u ON ts.doctor_id = u.id " +
                "JOIN doctors d ON ts.doctor_id = d.id " +
                "WHERE ts.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TimeSlot slot = slotRowMapper.mapRow(rs);
                    slot.setDoctorName(rs.getString("doctor_name"));
                    slot.setDoctorSpecialization(rs.getString("doctor_spec"));
                    return Optional.of(slot);
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding slot by id {}", slotId, e);
            throw new DatabaseException("Database error finding time slot", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<TimeSlot> findSlotByIdForUpdate(Connection conn, int slotId) throws DatabaseException {
        // Section 4.6: SELECT ... FOR UPDATE on slot row
        String sql = "SELECT * FROM time_slots WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, slotId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(slotRowMapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error finding slot for update {}", slotId, e);
            throw new DatabaseException("Database error acquiring row lock on time slot", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean markSlotBooked(Connection conn, int slotId, boolean isBooked) throws DatabaseException {
        String sql = "UPDATE time_slots SET is_booked = ?, version = version + 1 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isBooked);
            ps.setInt(2, slotId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating slot booked status for slot {}", slotId, e);
            throw new DatabaseException("Database error updating time slot status", e);
        }
    }

    @Override
    public TimeSlot saveSlot(TimeSlot slot) throws DatabaseException {
        String sql = "INSERT INTO time_slots (doctor_id, slot_date, start_time, end_time, is_booked, version) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, slot.getDoctorId());
            ps.setDate(2, Date.valueOf(slot.getSlotDate()));
            ps.setTime(3, Time.valueOf(slot.getStartTime()));
            ps.setTime(4, Time.valueOf(slot.getEndTime()));
            ps.setBoolean(5, slot.isBooked());
            ps.setInt(6, slot.getVersion());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    slot.setId(keys.getInt(1));
                }
            }
            return slot;
        } catch (SQLException e) {
            logger.error("Error saving slot for doctor {}", slot.getDoctorId(), e);
            throw new DatabaseException("Database error saving time slot", e);
        }
    }

    @Override
    public void generateSlotsForDoctor(int doctorId, LocalDate startDate, int daysAhead) throws DatabaseException {
        List<Schedule> schedules = findByDoctorId(doctorId);
        if (schedules.isEmpty()) return;

        String insertSql = "INSERT IGNORE INTO time_slots (doctor_id, slot_date, start_time, end_time, is_booked) VALUES (?, ?, ?, ?, FALSE)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertSql)) {
            conn.setAutoCommit(false);

            for (int i = 0; i < daysAhead; i++) {
                LocalDate curDate = startDate.plusDays(i);
                DayOfWeek dow = curDate.getDayOfWeek();

                for (Schedule sch : schedules) {
                    if (sch.getDayOfWeek().equalsIgnoreCase(dow.name())) {
                        LocalTime curTime = sch.getStartTime();
                        LocalTime endTime = sch.getEndTime();
                        int duration = sch.getSlotDurationMinutes() > 0 ? sch.getSlotDurationMinutes() : 30;

                        while (curTime.plusMinutes(duration).isBefore(endTime) || curTime.plusMinutes(duration).equals(endTime)) {
                            LocalTime slotEnd = curTime.plusMinutes(duration);
                            ps.setInt(1, doctorId);
                            ps.setDate(2, Date.valueOf(curDate));
                            ps.setTime(3, Time.valueOf(curTime));
                            ps.setTime(4, Time.valueOf(slotEnd));
                            ps.addBatch();
                            curTime = slotEnd;
                        }
                    }
                }
            }

            ps.executeBatch();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error("Error generating slots for doctor {}", doctorId, e);
            throw new DatabaseException("Database error generating bulk time slots", e);
        }
    }

    @Override
    public Schedule save(Schedule entity) throws DatabaseException {
        String sql = "INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, entity.getDoctorId());
            ps.setString(2, entity.getDayOfWeek());
            ps.setTime(3, Time.valueOf(entity.getStartTime()));
            ps.setTime(4, Time.valueOf(entity.getEndTime()));
            ps.setInt(5, entity.getSlotDurationMinutes());
            ps.setBoolean(6, entity.isActive());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getInt(1));
                }
            }
            return entity;
        } catch (SQLException e) {
            logger.error("Error saving schedule for doctor {}", entity.getDoctorId(), e);
            throw new DatabaseException("Database error saving schedule", e);
        }
    }

    @Override
    public boolean update(Schedule entity) throws DatabaseException {
        String sql = "UPDATE schedules SET day_of_week = ?, start_time = ?, end_time = ?, slot_duration_minutes = ?, is_active = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, entity.getDayOfWeek());
            ps.setTime(2, Time.valueOf(entity.getStartTime()));
            ps.setTime(3, Time.valueOf(entity.getEndTime()));
            ps.setInt(4, entity.getSlotDurationMinutes());
            ps.setBoolean(5, entity.isActive());
            ps.setInt(6, entity.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error updating schedule {}", entity.getId(), e);
            throw new DatabaseException("Database error updating schedule", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) throws DatabaseException {
        String sql = "DELETE FROM schedules WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error deleting schedule {}", id, e);
            throw new DatabaseException("Database error deleting schedule", e);
        }
    }
}
