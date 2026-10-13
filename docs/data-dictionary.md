# CareFirst Online Healthcare Management System — Data Dictionary & Schema Specification

**System:** Online Healthcare Management System (CareFirst)  
**Database Engine:** MySQL 8.0+ / 9.x (InnoDB Storage Engine)  
**Character Set / Collation:** `utf8mb4` / `utf8mb4_unicode_ci`  
**Review Assessment:** Review 1 — Database Design & JDBC Integration (3NF Compliance)

---

## 1. Schema Architecture & 3NF Normalization Analysis

The database schema is strictly designed following the **Third Normal Form (3NF)** rules to eliminate redundancy, avoid update anomalies, and guarantee referential integrity:

1. **First Normal Form (1NF):**
   - All column values are atomic (e.g., vitals split into `vitals_blood_pressure`, `vitals_heart_rate`, `vitals_temperature`).
   - Each table possesses a designated Primary Key (PK).
   - No repeating groups or multivalued arrays.
2. **Second Normal Form (2NF):**
   - All non-key attributes are fully functionally dependent on the primary key.
   - Separate entity tables isolate specialized attributes (e.g., `doctors` and `patients` extend `users` via 1-to-1 PK-to-FK mapping rather than denormalizing sparse columns into a monolithic table).
3. **Third Normal Form (3NF):**
   - Transitive dependencies are removed. For instance, `appointments` references `slot_id`, `doctor_id`, and `patient_id`; consultation fees are anchored in `doctors`, while diagnoses belong in `medical_records`.

---

## 2. Table-by-Table Data Dictionary

### 2.1 Table: `users`
**Purpose:** Base polymorphic identity table storing authentication credentials, authorization roles, and account security state for all system actors.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Unique surrogate user identifier. |
| `name` | `VARCHAR(100)` | NOT NULL | - | Full legal name of user. |
| `email` | `VARCHAR(150)` | NOT NULL, UNIQUE, INDEX (`idx_user_email`) | - | Login email identifier (case-insensitive unique). |
| `password_hash` | `VARCHAR(255)` | NOT NULL | - | BCrypt salted cryptographic hash ($2a$10$...). |
| `role` | `ENUM('ADMIN','DOCTOR','PATIENT')` | NOT NULL, INDEX (`idx_user_role`) | - | Role-based authorization principal. |
| `status` | `ENUM('ACTIVE','INACTIVE','LOCKED')` | NOT NULL | `'ACTIVE'` | Current account state. |
| `failed_attempts` | `INT` | NOT NULL | `0` | Consecutive failed login tally for lockout protection. |
| `locked_until` | `DATETIME` | NULL | `NULL` | Lockout expiration timestamp (triggers after 5 failed logins). |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | Audit account creation timestamp. |
| `updated_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last profile modification timestamp. |

---

### 2.2 Table: `doctors`
**Purpose:** Specialized table holding healthcare practitioner profiles, clinical credentials, consultation fees, and room assignments. 1-to-1 extension of `users`.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, FK &rarr; `users(id)` ON DELETE CASCADE | - | Doctor identifier matching parent user id. |
| `specialization` | `VARCHAR(100)` | NOT NULL, INDEX (`idx_doctor_spec`) | - | Medical specialty (e.g., Cardiology, Dermatology). |
| `experience` | `INT` | NOT NULL | `1` | Years of active medical practice. |
| `consultation_fee` | `DECIMAL(10,2)` | NOT NULL | `50.00` | Standard fee in USD charged per 30-min consultation. |
| `room_number` | `VARCHAR(50)` | NULL | `'101-A'` | Physical clinic examination room allocation. |
| `bio` | `TEXT` | NULL | `NULL` | Professional background and medical education biography. |

---

### 2.3 Table: `patients`
**Purpose:** Specialized table storing patient demographic, physical, and emergency contact records. 1-to-1 extension of `users`.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, FK &rarr; `users(id)` ON DELETE CASCADE | - | Patient identifier matching parent user id. |
| `age` | `INT` | NULL | `NULL` | Patient chronological age in years. |
| `gender` | `VARCHAR(20)` | NULL | `NULL` | Biological gender (Male, Female, Other). |
| `phone` | `VARCHAR(30)` | NULL, INDEX (`idx_patient_phone`) | `NULL` | Contact telephone / mobile number. |
| `blood_group` | `VARCHAR(10)` | NULL | `NULL` | ABO & Rh blood type (e.g., O+, A-, B+). |
| `address` | `TEXT` | NULL | `NULL` | Residential physical address. |
| `emergency_contact`| `VARCHAR(100)` | NULL | `NULL` | Emergency contact person name and telephone. |

---

### 2.4 Table: `schedules`
**Purpose:** Recurring weekly schedule template defining standard working hours and slot durations per doctor.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Schedule rule identifier. |
| `doctor_id` | `INT` | NOT NULL, FK &rarr; `doctors(id)` ON DELETE CASCADE, INDEX (`idx_schedule_doctor`) | - | Doctor to whom schedule applies. |
| `day_of_week` | `VARCHAR(20)` | NOT NULL | - | Day name: MONDAY, TUESDAY, WEDNESDAY, etc. |
| `start_time` | `TIME` | NOT NULL | - | Shift start time (e.g., 09:00:00). |
| `end_time` | `TIME` | NOT NULL | - | Shift end time (e.g., 17:00:00). |
| `slot_duration_minutes` | `INT` | NOT NULL | `30` | Duration per appointment slot in minutes. |
| `is_active` | `BOOLEAN` | NOT NULL | `TRUE` | Whether this recurring schedule is enabled. |

---

### 2.5 Table: `time_slots`
**Purpose:** Concrete, date-specific booking slots generated from doctor schedules, supporting atomic pessimistic and optimistic concurrency locks.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Unique slot surrogate identifier. |
| `doctor_id` | `INT` | NOT NULL, FK &rarr; `doctors(id)` ON DELETE CASCADE | - | Doctor conducting the slot. |
| `slot_date` | `DATE` | NOT NULL | - | Specific calendar date of the consultation. |
| `start_time` | `TIME` | NOT NULL | - | Slot start time. |
| `end_time` | `TIME` | NOT NULL | - | Slot completion time. |
| `is_booked` | `BOOLEAN` | NOT NULL, INDEX (`idx_slot_lookup`) | `FALSE` | Booking flag preventing double booking. |
| `version` | `INT` | NOT NULL | `0` | Optimistic concurrency control version counter. |

**Constraints & Indexes:**
- Composite Unique Key: `uq_doctor_slot (doctor_id, slot_date, start_time)` prevents generating duplicate slots for the same doctor at the same instant.
- Lookup Index: `idx_slot_lookup (doctor_id, slot_date, is_booked)` optimizes slot availability queries.

---

### 2.6 Table: `appointments`
**Purpose:** Central transaction ledger tracking patient bookings, doctor consultations, scheduling status, and clinical notes.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Appointment reference number. |
| `doctor_id` | `INT` | NOT NULL, FK &rarr; `doctors(id)`, INDEX (`idx_app_doctor_date`) | - | Assigned physician. |
| `patient_id` | `INT` | NOT NULL, FK &rarr; `patients(id)`, INDEX (`idx_app_patient`) | - | Registered patient. |
| `slot_id` | `INT` | NOT NULL, FK &rarr; `time_slots(id)`, UNIQUE (`uq_appointment_slot`) | - | Reserved concrete time slot (1-to-1). |
| `appointment_date`| `DATE` | NOT NULL | - | Date of consultation. |
| `start_time` | `TIME` | NOT NULL | - | Consultation start time. |
| `end_time` | `TIME` | NOT NULL | - | Consultation end time. |
| `status` | `ENUM('PENDING','CONFIRMED','COMPLETED','CANCELLED','RESCHEDULED')` | NOT NULL, INDEX (`idx_app_status`) | `'CONFIRMED'` | Operational workflow status. |
| `reason` | `VARCHAR(255)` | NOT NULL | - | Chief patient complaint or visit objective. |
| `notes` | `TEXT` | NULL | `NULL` | Preliminary doctor / triage notes. |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | Booking creation timestamp. |
| `updated_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Status change timestamp. |

---

### 2.7 Table: `medical_records`
**Purpose:** Electronic Health Record (EHR) storing finalized clinical diagnoses, vitals, prescriptions, and physician observations post-consultation.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Unique medical record record ID. |
| `appointment_id` | `INT` | NULL, FK &rarr; `appointments(id)` ON DELETE SET NULL | `NULL` | Optional link to associated consultation appointment. |
| `patient_id` | `INT` | NOT NULL, FK &rarr; `patients(id)` ON DELETE CASCADE, INDEX (`idx_record_patient`) | - | Patient whose EHR chart this belongs to. |
| `doctor_id` | `INT` | NOT NULL, FK &rarr; `doctors(id)` | - | Authoring physician who signed the record. |
| `diagnosis` | `TEXT` | NOT NULL | - | Official medical diagnosis and ICD evaluation. |
| `prescription` | `TEXT` | NULL | `NULL` | Dosage, drug name, frequency instructions. |
| `vitals_blood_pressure` | `VARCHAR(30)` | NULL | `NULL` | Systolic/diastolic blood pressure (e.g., 120/80 mmHg). |
| `vitals_heart_rate` | `VARCHAR(30)` | NULL | `NULL` | Pulse rate in beats per minute (e.g., 72 bpm). |
| `vitals_temperature` | `VARCHAR(30)` | NULL | `NULL` | Body temperature (e.g., 98.6 F). |
| `notes` | `TEXT` | NULL | `NULL` | Physician clinical observations & follow-up plan. |
| `record_date` | `DATE` | NOT NULL | - | Encounter date. |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | Record seal timestamp. |

---

### 2.8 Table: `feedback`
**Purpose:** Patient evaluations and quality ratings (1 to 5 stars) linked to completed appointments.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Unique feedback record identifier. |
| `appointment_id` | `INT` | NOT NULL, UNIQUE, FK &rarr; `appointments(id)` ON DELETE CASCADE | - | Single review per completed appointment. |
| `doctor_id` | `INT` | NOT NULL, FK &rarr; `doctors(id)`, INDEX (`idx_feedback_doctor`) | - | Evaluated doctor. |
| `patient_id` | `INT` | NOT NULL, FK &rarr; `patients(id)` | - | Submitting patient. |
| `rating` | `INT` | NOT NULL, `CHECK (rating >= 1 AND rating <= 5)` | - | Star satisfaction rating (1 to 5). |
| `comment` | `TEXT` | NULL | `NULL` | Written subjective review and feedback. |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | Feedback submission timestamp. |

---

### 2.9 Table: `notifications`
**Purpose:** In-app real-time alerts, appointment reminders, and administrative notifications delivered to users.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Notification identifier. |
| `user_id` | `INT` | NOT NULL, FK &rarr; `users(id)` ON DELETE CASCADE | - | Recipient user. |
| `title` | `VARCHAR(150)` | NOT NULL | - | Notification headline. |
| `message` | `TEXT` | NOT NULL | - | Detailed message body. |
| `type` | `VARCHAR(50)` | NOT NULL | `'INFO'` | Classification (APPOINTMENT, REMINDER, SYSTEM). |
| `is_read` | `BOOLEAN` | NOT NULL, INDEX (`idx_notif_user_read`) | `FALSE` | Read status toggle. |
| `created_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | Dispatch timestamp. |

---

### 2.10 Table: `system_settings`
**Purpose:** Key-value configuration parameters dynamically managed by system administrators.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `setting_key` | `VARCHAR(100)` | PK, NOT NULL | - | Unique system property identifier. |
| `setting_value` | `TEXT` | NOT NULL | - | Property setting value. |
| `description` | `VARCHAR(255)` | NULL | `NULL` | Human-readable explanation of setting impact. |
| `updated_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Last updated timestamp. |

---

### 2.11 Table: `audit_logs`
**Purpose:** Immutable security and compliance trail recording critical user operations, IP addresses, and events.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Unique audit log ID. |
| `user_id` | `INT` | NULL, FK &rarr; `users(id)` ON DELETE SET NULL | `NULL` | Actor user ID (or NULL if anonymous). |
| `action` | `VARCHAR(100)` | NOT NULL | - | Event identifier (LOGIN, LOGOUT, BOOK_APPOINTMENT, etc.). |
| `details` | `TEXT` | NULL | `NULL` | Operational context, parameters, or failure reasons. |
| `ip_address` | `VARCHAR(45)` | NULL | `NULL` | IPv4 or IPv6 client network origin. |
| `created_at` | `TIMESTAMP` | NOT NULL, INDEX (`idx_audit_time`) | `CURRENT_TIMESTAMP` | Immutable log recording timestamp. |

---

### 2.12 Table: `login_attempts`
**Purpose:** Brute-force mitigation tracking security attempts for credential rate limiting and account lockout.

| Column Name | Data Type | Constraints | Default | Purpose / Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT` | PK, AUTO_INCREMENT, NOT NULL | Auto | Unique attempt tracking ID. |
| `email` | `VARCHAR(150)` | NOT NULL, INDEX (`idx_attempt_email`) | - | Attempted login username. |
| `ip_address` | `VARCHAR(45)` | NULL | `NULL` | Origin IP of login attempt. |
| `success` | `BOOLEAN` | NOT NULL | `FALSE` | Outcome: TRUE for success, FALSE for failure. |
| `attempted_at` | `TIMESTAMP` | NOT NULL | `CURRENT_TIMESTAMP` | Exact timestamp of attempt. |

---

## 3. Referential Integrity & Foreign Key Relationship Matrix

| Foreign Key Constraint | Source Table & Column | Referenced Table & Column | Cascade Action |
| :--- | :--- | :--- | :--- |
| `fk_doctor_user` | `doctors.id` | `users.id` | `ON DELETE CASCADE` |
| `fk_patient_user` | `patients.id` | `users.id` | `ON DELETE CASCADE` |
| `fk_schedule_doctor` | `schedules.doctor_id` | `doctors.id` | `ON DELETE CASCADE` |
| `fk_slot_doctor` | `time_slots.doctor_id` | `doctors.id` | `ON DELETE CASCADE` |
| `fk_appointment_doctor` | `appointments.doctor_id` | `doctors.id` | `RESTRICT` |
| `fk_appointment_patient` | `appointments.patient_id`| `patients.id` | `RESTRICT` |
| `fk_appointment_slot` | `appointments.slot_id` | `time_slots.id` | `RESTRICT` |
| `fk_record_app` | `medical_records.appointment_id` | `appointments.id` | `ON DELETE SET NULL` |
| `fk_record_patient` | `medical_records.patient_id` | `patients.id` | `ON DELETE CASCADE` |
| `fk_record_doctor` | `medical_records.doctor_id` | `doctors.id` | `RESTRICT` |
| `fk_feedback_app` | `feedback.appointment_id` | `appointments.id` | `ON DELETE CASCADE` |
| `fk_feedback_doctor` | `feedback.doctor_id` | `doctors.id` | `RESTRICT` |
| `fk_feedback_patient` | `feedback.patient_id` | `patients.id` | `RESTRICT` |
| `fk_notif_user` | `notifications.user_id` | `users.id` | `ON DELETE CASCADE` |
| `fk_audit_user` | `audit_logs.user_id` | `users.id` | `ON DELETE SET NULL` |
