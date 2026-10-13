-- ==============================================================================
-- Health Management System (HMS) Database Schema
-- Compatible with MySQL 8.0 and H2 (MySQL Mode)
-- ==============================================================================

-- Drop tables in reverse order of foreign keys for clean recreation if needed
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS invoice_items;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS diagnostic_tests;
DROP TABLE IF EXISTS diagnostic_services;
DROP TABLE IF EXISTS prescriptions;
DROP TABLE IF EXISTS medical_records;
DROP TABLE IF EXISTS appointments;
DROP TABLE IF EXISTS doctors;
DROP TABLE IF EXISTS patients;

-- 1. Patients Table
CREATE TABLE patients (
    patient_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE NOT NULL,
    gender VARCHAR(20) NOT NULL,
    blood_group VARCHAR(10),
    phone VARCHAR(25) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(25),
    allergies VARCHAR(500),
    medical_history VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Doctors Table
CREATE TABLE doctors (
    doctor_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    qualification VARCHAR(100) NOT NULL,
    experience_years INT DEFAULT 0,
    consulting_fee DECIMAL(10,2) NOT NULL,
    phone VARCHAR(25) NOT NULL,
    email VARCHAR(100),
    available_days VARCHAR(100) DEFAULT 'Mon, Tue, Wed, Thu, Fri',
    available_hours VARCHAR(100) DEFAULT '09:00 AM - 05:00 PM',
    status VARCHAR(20) DEFAULT 'ACTIVE'
);

-- 3. Appointments Table
CREATE TABLE appointments (
    appointment_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    appointment_time VARCHAR(20) NOT NULL,
    reason_for_visit VARCHAR(500),
    status VARCHAR(30) DEFAULT 'SCHEDULED',
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
);

-- 4. Medical Records Table
CREATE TABLE medical_records (
    record_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    doctor_id INT NOT NULL,
    appointment_id INT,
    record_date DATE NOT NULL,
    diagnosis VARCHAR(255) NOT NULL,
    symptoms VARCHAR(500),
    blood_pressure VARCHAR(20),
    heart_rate INT,
    temperature DECIMAL(4,1),
    weight_kg DECIMAL(5,1),
    treatment_plan VARCHAR(1000),
    doctor_notes VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id) ON DELETE CASCADE
);

-- 5. Prescriptions Table
CREATE TABLE prescriptions (
    prescription_id INT AUTO_INCREMENT PRIMARY KEY,
    record_id INT NOT NULL,
    medicine_name VARCHAR(150) NOT NULL,
    dosage VARCHAR(100) NOT NULL,
    frequency VARCHAR(100) NOT NULL,
    duration VARCHAR(50) NOT NULL,
    instructions VARCHAR(255),
    FOREIGN KEY (record_id) REFERENCES medical_records(record_id) ON DELETE CASCADE
);

-- 6. Diagnostic Services Catalog
CREATE TABLE diagnostic_services (
    service_id INT AUTO_INCREMENT PRIMARY KEY,
    service_name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    cost DECIMAL(10,2) NOT NULL,
    turnaround_hours INT DEFAULT 24,
    description VARCHAR(500)
);

-- 7. Diagnostic Test Bookings & Results
CREATE TABLE diagnostic_tests (
    test_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    service_id INT NOT NULL,
    doctor_id INT,
    appointment_id INT,
    booking_date DATE NOT NULL,
    status VARCHAR(30) DEFAULT 'ORDERED',
    results VARCHAR(1000),
    normal_range VARCHAR(100),
    remarks VARCHAR(500),
    completed_at TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (service_id) REFERENCES diagnostic_services(service_id) ON DELETE CASCADE
);

-- 8. Invoices Table
CREATE TABLE invoices (
    invoice_id INT AUTO_INCREMENT PRIMARY KEY,
    patient_id INT NOT NULL,
    appointment_id INT,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_date DATE NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    tax DECIMAL(10,2) DEFAULT 0.00,
    discount DECIMAL(10,2) DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL,
    payment_status VARCHAR(30) DEFAULT 'UNPAID',
    payment_method VARCHAR(50),
    paid_amount DECIMAL(10,2) DEFAULT 0.00,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE
);

-- 9. Invoice Line Items
CREATE TABLE invoice_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    quantity INT DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE
);

-- 10. Edit-History / Audit Trail Table (Data Traceability)
CREATE TABLE audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id INT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    field_name VARCHAR(100),
    old_value VARCHAR(1000),
    new_value VARCHAR(1000),
    modified_by VARCHAR(100) DEFAULT 'Healthcare Staff',
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reason_or_notes VARCHAR(500)
);
