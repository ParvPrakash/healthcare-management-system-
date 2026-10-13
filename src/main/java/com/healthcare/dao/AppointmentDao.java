package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentDao extends Repository<Appointment, Integer> {
    // Compile-time Polymorphism: Method Overloading for searchAppointments
    List<Appointment> searchAppointments(int patientId, AppointmentStatus status) throws DatabaseException;
    List<Appointment> searchAppointments(int doctorId, LocalDate date) throws DatabaseException;
    List<Appointment> searchAppointments(String query, AppointmentStatus status, int limit, int offset) throws DatabaseException;

    List<Appointment> findUpcomingByDoctor(int doctorId, int limit) throws DatabaseException;
    List<Appointment> findPastByDoctor(int doctorId, int limit) throws DatabaseException;
    List<Appointment> findByPatientId(int patientId) throws DatabaseException;
    List<Appointment> findByDoctorId(int doctorId) throws DatabaseException;
    List<Appointment> findTodayAppointments() throws DatabaseException;
    int countTotal() throws DatabaseException;
    int countToday() throws DatabaseException;
    int countByStatus(AppointmentStatus status) throws DatabaseException;

    // Transaction-aware methods
    Appointment saveTransactional(Connection conn, Appointment appointment) throws DatabaseException;
    boolean updateStatusTransactional(Connection conn, int appointmentId, AppointmentStatus status) throws DatabaseException;
    boolean rescheduleTransactional(Connection conn, int appointmentId, int newSlotId, LocalDate newDate, LocalTime newStart, LocalTime newEnd) throws DatabaseException;
}
