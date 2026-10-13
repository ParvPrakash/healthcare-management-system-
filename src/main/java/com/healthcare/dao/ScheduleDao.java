package com.healthcare.dao;

import com.healthcare.exception.DatabaseException;
import com.healthcare.model.Schedule;
import com.healthcare.model.TimeSlot;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleDao extends Repository<Schedule, Integer> {
    List<Schedule> findByDoctorId(int doctorId) throws DatabaseException;
    void deleteByDoctorId(int doctorId) throws DatabaseException;

    // Time Slot methods
    List<TimeSlot> findSlotsByDoctorAndDate(int doctorId, LocalDate date) throws DatabaseException;
    Optional<TimeSlot> findSlotById(int slotId) throws DatabaseException;
    TimeSlot saveSlot(TimeSlot slot) throws DatabaseException;
    void generateSlotsForDoctor(int doctorId, LocalDate startDate, int daysAhead) throws DatabaseException;

    // Transaction-managed slot locking methods
    Optional<TimeSlot> findSlotByIdForUpdate(Connection conn, int slotId) throws DatabaseException;
    boolean markSlotBooked(Connection conn, int slotId, boolean isBooked) throws DatabaseException;
}
