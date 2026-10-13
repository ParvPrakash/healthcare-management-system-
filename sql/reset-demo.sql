-- ========================================================
-- Reset Demo Data for Online Healthcare Management System
-- Restores all tables and pristine demo data
-- ========================================================

CREATE DATABASE IF NOT EXISTS healthcare_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE healthcare_db;

-- ========================================================
-- Online Healthcare Management System Schema
-- MySQL 8.0+ Compatible
-- ========================================================

DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS time_slots;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS patients;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS system_settings;
DROP TABLE IF EXISTS users;

-- 1. Users table (Base table for polymorphism)
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'DOCTOR', 'PATIENT') NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE', 'LOCKED') NOT NULL DEFAULT 'ACTIVE',
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Doctors table (Specialized role details)
CREATE TABLE doctors (
    id INT PRIMARY KEY,
    specialization VARCHAR(100) NOT NULL,
    experience INT NOT NULL DEFAULT 1,
    consultation_fee DECIMAL(10,2) NOT NULL DEFAULT 50.00,
    room_number VARCHAR(50) DEFAULT '101-A',
    bio TEXT,
    CONSTRAINT fk_doctor_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_doctor_spec (specialization)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Patients table (Specialized role details)
CREATE TABLE patients (
    id INT PRIMARY KEY,
    age INT,
    gender VARCHAR(20),
    phone VARCHAR(30),
    blood_group VARCHAR(10),
    address TEXT,
    emergency_contact VARCHAR(100),
    CONSTRAINT fk_patient_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_patient_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Schedules table (Doctor recurring weekly template)
CREATE TABLE schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL, -- MONDAY, TUESDAY, etc.
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_schedule_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    INDEX idx_schedule_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Time Slots table (Concrete booking slots)
CREATE TABLE time_slots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    slot_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE,
    UNIQUE KEY uq_doctor_slot (doctor_id, slot_date, start_time),
    INDEX idx_slot_lookup (doctor_id, slot_date, is_booked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Appointments table
CREATE TABLE appointments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    slot_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED', 'RESCHEDULED') NOT NULL DEFAULT 'CONFIRMED',
    reason VARCHAR(255) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    CONSTRAINT fk_appointment_slot FOREIGN KEY (slot_id) REFERENCES time_slots(id),
    UNIQUE KEY uq_appointment_slot (slot_id),
    INDEX idx_app_doctor_date (doctor_id, appointment_date),
    INDEX idx_app_patient (patient_id),
    INDEX idx_app_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Medical Records table
CREATE TABLE medical_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NULL,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    diagnosis TEXT NOT NULL,
    prescription TEXT,
    vitals_blood_pressure VARCHAR(30),
    vitals_heart_rate VARCHAR(30),
    vitals_temperature VARCHAR(30),
    notes TEXT,
    record_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_record_app FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE SET NULL,
    CONSTRAINT fk_record_patient FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    CONSTRAINT fk_record_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    INDEX idx_record_patient (patient_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Feedback table (Ratings and reviews)
CREATE TABLE feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    appointment_id INT NOT NULL UNIQUE,
    doctor_id INT NOT NULL,
    patient_id INT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_feedback_app FOREIGN KEY (appointment_id) REFERENCES appointments(id) ON DELETE CASCADE,
    CONSTRAINT fk_feedback_doctor FOREIGN KEY (doctor_id) REFERENCES doctors(id),
    CONSTRAINT fk_feedback_patient FOREIGN KEY (patient_id) REFERENCES patients(id),
    INDEX idx_feedback_doctor (doctor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Notifications table
CREATE TABLE notifications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL DEFAULT 'INFO',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notif_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. System Settings table
CREATE TABLE system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    description VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Audit Logs table
CREATE TABLE audit_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_time (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Login Attempts table (For rate limiting & lockout after 5 failures)
CREATE TABLE login_attempts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(150) NOT NULL,
    ip_address VARCHAR(45),
    success BOOLEAN NOT NULL DEFAULT FALSE,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_attempt_email (email, attempted_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- ========================================================
-- Seed Data for Online Healthcare Management System
-- Passwords:
-- Admin:    Admin@123   (Hash: $2a$10$jOIpd52LOThNfuopGAa3lurQFfQ9GSspqMIlaeVh2M1MVOhyc5F1.)
-- Doctors:  Doctor@123  (Hash: $2a$10$uPbpLea5n1iZTe6EtqptHOqxKNSvVGSQiS0CYZ8CvFDqBA765bjYW)
-- Patients: Patient@123 (Hash: $2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W)
-- ========================================================

-- 1. Insert Users
INSERT INTO users (id, name, email, password_hash, role, status) VALUES
(1, 'System Administrator', 'admin@carefirst.com', '$2a$10$jOIpd52LOThNfuopGAa3lurQFfQ9GSspqMIlaeVh2M1MVOhyc5F1.', 'ADMIN', 'ACTIVE'),

(2, 'Dr. Sarah Jenkins', 'doctor.sarah@carefirst.com', '$2a$10$uPbpLea5n1iZTe6EtqptHOqxKNSvVGSQiS0CYZ8CvFDqBA765bjYW', 'DOCTOR', 'ACTIVE'),
(3, 'Dr. Marcus Vance', 'doctor.marcus@carefirst.com', '$2a$10$uPbpLea5n1iZTe6EtqptHOqxKNSvVGSQiS0CYZ8CvFDqBA765bjYW', 'DOCTOR', 'ACTIVE'),
(4, 'Dr. Elena Rostova', 'doctor.elena@carefirst.com', '$2a$10$uPbpLea5n1iZTe6EtqptHOqxKNSvVGSQiS0CYZ8CvFDqBA765bjYW', 'DOCTOR', 'ACTIVE'),
(5, 'Dr. Arjun Mehta', 'doctor.arjun@carefirst.com', '$2a$10$uPbpLea5n1iZTe6EtqptHOqxKNSvVGSQiS0CYZ8CvFDqBA765bjYW', 'DOCTOR', 'ACTIVE'),
(6, 'Dr. Chloe Bennett', 'doctor.chloe@carefirst.com', '$2a$10$uPbpLea5n1iZTe6EtqptHOqxKNSvVGSQiS0CYZ8CvFDqBA765bjYW', 'DOCTOR', 'ACTIVE'),

(7, 'John Doe', 'john.doe@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(8, 'Emily Watson', 'emily.watson@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(9, 'Michael Brown', 'michael.b@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(10, 'Jessica Taylor', 'jessica.t@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(11, 'David Miller', 'david.m@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(12, 'Sophia Anderson', 'sophia.a@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(13, 'James Wilson', 'james.w@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(14, 'Olivia Martinez', 'olivia.m@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(15, 'Robert Thomas', 'robert.t@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE'),
(16, 'Isabella Garcia', 'isabella.g@example.com', '$2a$10$nSk39N8SYPN0CWzAMW5iSujHKgt44Co8mC56fEPJDwEnzR84AHQ6W', 'PATIENT', 'ACTIVE');

-- 2. Insert Doctors Details
INSERT INTO doctors (id, specialization, experience, consultation_fee, room_number, bio) VALUES
(2, 'Cardiology', 12, 85.00, 'Suite 301', 'Board-certified cardiologist specializing in preventive cardiology, hypertension, and cardiovascular imaging.'),
(3, 'Neurology', 15, 110.00, 'Suite 405', 'Senior neurologist with expertise in migraine management, cognitive memory disorders, and stroke rehabilitation.'),
(4, 'Pediatrics', 8, 65.00, 'Suite 102', 'Caring pediatrician focused on holistic child development, childhood immunization, and pediatric asthma.'),
(5, 'Orthopedics', 10, 75.00, 'Suite 204', 'Orthopedic specialist in joint preservation, sports injury arthroscopy, and spine rehabilitation.'),
(6, 'Dermatology', 7, 70.00, 'Suite 108', 'Clinical dermatologist dedicated to acne, eczema management, and non-invasive dermatological procedures.');

-- 3. Insert Patients Details
INSERT INTO patients (id, age, gender, phone, blood_group, address, emergency_contact) VALUES
(7, 34, 'Male', '+1 555-0101', 'O+', '124 Maple Street, Springfield', 'Jane Doe (+1 555-0102)'),
(8, 29, 'Female', '+1 555-0103', 'A+', '742 Evergreen Terrace, Springfield', 'Arthur Watson (+1 555-0104)'),
(9, 45, 'Male', '+1 555-0105', 'B+', '89 Oak Lane, Riverside', 'Sarah Brown (+1 555-0106)'),
(10, 38, 'Female', '+1 555-0107', 'AB+', '45 Pine Street, Greenfield', 'Robert Taylor (+1 555-0108)'),
(11, 52, 'Male', '+1 555-0109', 'O-', '321 Elm Drive, Riverdale', 'Laura Miller (+1 555-0110)'),
(12, 26, 'Female', '+1 555-0111', 'A-', '902 Birch Road, Hillcrest', 'David Anderson (+1 555-0112)'),
(13, 41, 'Male', '+1 555-0113', 'B-', '15 Cedar Avenue, Highland', 'Claire Wilson (+1 555-0114)'),
(14, 31, 'Female', '+1 555-0115', 'O+', '67 Magnolia Blvd, Sunnyvale', 'Carlos Martinez (+1 555-0116)'),
(15, 60, 'Male', '+1 555-0117', 'AB-', '108 Willow Creek, Lakewood', 'Mary Thomas (+1 555-0118)'),
(16, 24, 'Female', '+1 555-0119', 'A+', '512 Sunset Vista, Pasadena', 'Maria Garcia (+1 555-0120)');

-- 4. Weekly Recurring Schedules
INSERT INTO schedules (doctor_id, day_of_week, start_time, end_time, slot_duration_minutes, is_active) VALUES
(2, 'MONDAY', '09:00:00', '17:00:00', 30, TRUE),
(2, 'TUESDAY', '09:00:00', '17:00:00', 30, TRUE),
(2, 'WEDNESDAY', '09:00:00', '17:00:00', 30, TRUE),
(2, 'THURSDAY', '09:00:00', '17:00:00', 30, TRUE),
(2, 'FRIDAY', '09:00:00', '17:00:00', 30, TRUE),

(3, 'MONDAY', '10:00:00', '18:00:00', 30, TRUE),
(3, 'TUESDAY', '10:00:00', '18:00:00', 30, TRUE),
(3, 'WEDNESDAY', '10:00:00', '18:00:00', 30, TRUE),
(3, 'THURSDAY', '10:00:00', '18:00:00', 30, TRUE),
(3, 'FRIDAY', '10:00:00', '18:00:00', 30, TRUE),

(4, 'MONDAY', '08:30:00', '16:30:00', 30, TRUE),
(4, 'TUESDAY', '08:30:00', '16:30:00', 30, TRUE),
(4, 'WEDNESDAY', '08:30:00', '16:30:00', 30, TRUE),
(4, 'THURSDAY', '08:30:00', '16:30:00', 30, TRUE),
(4, 'FRIDAY', '08:30:00', '16:30:00', 30, TRUE),

(5, 'MONDAY', '09:00:00', '17:00:00', 30, TRUE),
(5, 'WEDNESDAY', '09:00:00', '17:00:00', 30, TRUE),
(5, 'FRIDAY', '09:00:00', '17:00:00', 30, TRUE),

(6, 'TUESDAY', '09:00:00', '17:00:00', 30, TRUE),
(6, 'THURSDAY', '09:00:00', '17:00:00', 30, TRUE),
(6, 'FRIDAY', '09:00:00', '17:00:00', 30, TRUE);

-- 5. Time Slots (Today and Next 7 Days)
INSERT INTO time_slots (id, doctor_id, slot_date, start_time, end_time, is_booked) VALUES
-- Doctor 2 Today
(1, 2, CURRENT_DATE, '09:00:00', '09:30:00', TRUE),
(2, 2, CURRENT_DATE, '09:30:00', '10:00:00', TRUE),
(3, 2, CURRENT_DATE, '10:00:00', '10:30:00', FALSE),
(4, 2, CURRENT_DATE, '10:30:00', '11:00:00', FALSE),
(5, 2, CURRENT_DATE, '11:00:00', '11:30:00', FALSE),
(6, 2, CURRENT_DATE, '14:00:00', '14:30:00', FALSE),
(7, 2, CURRENT_DATE, '14:30:00', '15:00:00', FALSE),

-- Doctor 2 Tomorrow
(8, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '09:00:00', '09:30:00', FALSE),
(9, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00', '10:00:00', FALSE),
(10, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00', '10:30:00', FALSE),
(11, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:30:00', '11:00:00', FALSE),
(12, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:00:00', '14:30:00', FALSE),

-- Doctor 3 Today
(13, 3, CURRENT_DATE, '10:00:00', '10:30:00', TRUE),
(14, 3, CURRENT_DATE, '10:30:00', '11:00:00', FALSE),
(15, 3, CURRENT_DATE, '11:00:00', '11:30:00', FALSE),
(16, 3, CURRENT_DATE, '15:00:00', '15:30:00', FALSE),

-- Doctor 4 Today
(17, 4, CURRENT_DATE, '09:00:00', '09:30:00', TRUE),
(18, 4, CURRENT_DATE, '09:30:00', '10:00:00', FALSE),
(19, 4, CURRENT_DATE, '11:00:00', '11:30:00', FALSE),

-- Doctor 5 Today
(20, 5, CURRENT_DATE, '09:30:00', '10:00:00', TRUE),
(21, 5, CURRENT_DATE, '10:00:00', '10:30:00', FALSE),
(22, 5, CURRENT_DATE, '14:00:00', '14:30:00', FALSE),

-- Doctor 6 Today
(23, 6, CURRENT_DATE, '10:00:00', '10:30:00', TRUE),
(24, 6, CURRENT_DATE, '10:30:00', '11:00:00', FALSE),
(25, 6, CURRENT_DATE, '15:00:00', '15:30:00', FALSE),

-- Past slots for completed appointments
(26, 2, DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '10:00:00', '10:30:00', TRUE),
(27, 3, DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '11:00:00', '11:30:00', TRUE),
(28, 4, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:00:00', '14:30:00', TRUE),
(29, 5, DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '15:00:00', '15:30:00', TRUE),
(30, 6, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00', '10:00:00', TRUE);

-- 6. Insert Appointments
INSERT INTO appointments (id, doctor_id, patient_id, slot_id, appointment_date, start_time, end_time, status, reason, notes) VALUES
(1, 2, 7, 1, CURRENT_DATE, '09:00:00', '09:30:00', 'CONFIRMED', 'Routine cardiac health screening', 'Patient reports mild shortness of breath upon exertion.'),
(2, 2, 8, 2, CURRENT_DATE, '09:30:00', '10:00:00', 'CONFIRMED', 'High blood pressure follow-up', 'Follow-up on lisinopril medication tolerance.'),
(3, 3, 9, 13, CURRENT_DATE, '10:00:00', '10:30:00', 'CONFIRMED', 'Persistent tension migraine headache', 'Episodes lasting 3-4 days per week.'),
(4, 4, 10, 17, CURRENT_DATE, '09:00:00', '09:30:00', 'CONFIRMED', 'Childhood immunization checkup', 'Yearly wellness checkup for toddler.'),
(5, 5, 11, 20, CURRENT_DATE, '09:30:00', '10:00:00', 'CONFIRMED', 'Right knee arthroscopy follow-up', 'Evaluation of postoperative range of motion.'),
(6, 6, 12, 23, CURRENT_DATE, '10:00:00', '10:30:00', 'CONFIRMED', 'Facial dermatitis & skin rash', 'Evaluation of seasonal eczema flare-up.'),

-- Completed historical appointments
(7, 2, 13, 26, DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY), '10:00:00', '10:30:00', 'COMPLETED', 'Chest discomfort evaluation', 'ECG normal, stress test recommended.'),
(8, 3, 14, 27, DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY), '11:00:00', '11:30:00', 'COMPLETED', 'Neurological neuropathy assessment', 'Nerve conduction tests normal.'),
(9, 4, 15, 28, DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY), '14:00:00', '14:30:00', 'COMPLETED', 'Pediatric allergic reaction', 'Prescribed antihistamines and allergen avoidance.'),
(10, 5, 16, 29, DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY), '15:00:00', '15:30:00', 'COMPLETED', 'Left shoulder rotator cuff strain', 'Physical therapy plan initiated.'),
(11, 6, 7, 30, DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00', '10:00:00', 'COMPLETED', 'Skin lesion examination', 'Benign keratosis identified.');

-- 7. Insert Medical Records for Completed Visits
INSERT INTO medical_records (id, appointment_id, patient_id, doctor_id, diagnosis, prescription, vitals_blood_pressure, vitals_heart_rate, vitals_temperature, notes, record_date) VALUES
(1, 7, 13, 2, 'Mild Essential Hypertension, Grade 1', 'Amlodipine 5mg once daily, reduce sodium intake', '138/88 mmHg', '74 bpm', '98.4 F', 'Patient advised to monitor home BP readings daily and return in 4 weeks.', DATE_SUB(CURRENT_DATE, INTERVAL 3 DAY)),
(2, 8, 14, 3, 'Peripheral Neuropraxia of Upper Extremity', 'Gabapentin 100mg nightly, Vitamin B-Complex', '122/76 mmHg', '68 bpm', '98.6 F', 'Ergonomic keyboard adjustment recommended. Follow up if tingling persists.', DATE_SUB(CURRENT_DATE, INTERVAL 4 DAY)),
(3, 9, 15, 4, 'Acute Urticaria (Allergic Reaction)', 'Cetirizine 10mg once daily for 7 days, Calamine topical', '110/70 mmHg', '82 bpm', '98.8 F', 'No airway involvement. Advised food diary log.', DATE_SUB(CURRENT_DATE, INTERVAL 2 DAY)),
(4, 10, 16, 5, 'Subacromial Impingement Syndrome', 'Naproxen 500mg twice daily with meals, Physical Therapy', '126/80 mmHg', '72 bpm', '98.2 F', 'Patient demonstrated rotator cuff strengthening exercises.', DATE_SUB(CURRENT_DATE, INTERVAL 5 DAY)),
(5, 11, 7, 6, 'Seborrheic Keratosis, Non-Malignant', 'Gentle non-soap cleanser, SPF 50 sunscreen daily', '120/78 mmHg', '70 bpm', '98.5 F', 'Biopsy not indicated. Sun safety precautions reviewed.', DATE_SUB(CURRENT_DATE, INTERVAL 1 DAY));

-- 8. Insert Patient Feedback & Ratings
INSERT INTO feedback (id, appointment_id, doctor_id, patient_id, rating, comment) VALUES
(1, 7, 2, 13, 5, 'Dr. Sarah was exceptionally thorough and explained every detail of my cardiovascular results with warmth and clarity.'),
(2, 8, 3, 14, 5, 'Dr. Vance listened with great patience. His diagnosis was prompt and the prescribed therapy has significantly reduced my migraines.'),
(3, 9, 4, 15, 5, 'Dr. Elena was wonderful with my child. Warm, attentive, and very reassuring throughout the consultation.'),
(4, 10, 5, 16, 4, 'Very knowledgeable orthopedic surgeon. Clear rehabilitation plan and practical advice.'),
(5, 11, 6, 7, 5, 'Dr. Chloe is stellar! She took immediate care of my skin concern and made me feel completely comfortable.');

-- 9. System Settings
INSERT INTO system_settings (setting_key, setting_value, description) VALUES
('clinic_name', 'CareFirst Health Center', 'Public official clinic display name'),
('clinic_phone', '+1 (800) 555-CARE', 'Clinic emergency and inquiry telephone'),
('clinic_email', 'contact@carefirst.com', 'Primary administrative contact email'),
('clinic_address', '742 Healthcare Parkway, Suite 100, Metro City, MC 90210', 'Physical clinic address'),
('slot_duration_minutes', '30', 'Standard clinical appointment slot duration'),
('working_hours_start', '08:00', 'Clinic opening time'),
('working_hours_end', '18:00', 'Clinic closing time'),
('session_timeout_minutes', '30', 'Inactivity duration before automatic logout'),
('max_bookings_per_day', '8', 'Maximum appointments per doctor daily limit'),
('notifications_enabled', 'true', 'Global switch for email and in-app notifications'),
('maintenance_mode', 'false', 'Flag for system maintenance outages');

-- 10. Sample Notifications
INSERT INTO notifications (user_id, title, message, type, is_read) VALUES
(7, 'Appointment Confirmed', 'Your consultation with Dr. Sarah Jenkins is scheduled for today at 09:00 AM.', 'SUCCESS', FALSE),
(8, 'Appointment Confirmed', 'Your consultation with Dr. Sarah Jenkins is scheduled for today at 09:30 AM.', 'SUCCESS', FALSE),
(2, 'Schedule Update', 'You have 2 confirmed patient consultations scheduled for today.', 'INFO', FALSE),
(1, 'System Health Check', 'Database connection pool and background schedulers are running smoothly.', 'INFO', TRUE);

-- 11. Initial Audit Logs
INSERT INTO audit_logs (user_id, action, details, ip_address) VALUES
(1, 'SYSTEM_INIT', 'Database schema initialized and seed records loaded.', '127.0.0.1'),
(1, 'SETTINGS_LOAD', 'Clinic configuration parameters loaded successfully.', '127.0.0.1'),
(7, 'BOOK_APPOINTMENT', 'Patient John Doe booked consultation with Dr. Sarah Jenkins', '127.0.0.1');
