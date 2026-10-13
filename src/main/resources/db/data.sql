-- ==============================================================================
-- Initial Seed Data for Health Management System
-- ==============================================================================

-- 1. Insert Sample Patients
INSERT INTO patients (first_name, last_name, dob, gender, blood_group, phone, email, address, emergency_contact_name, emergency_contact_phone, allergies, medical_history) VALUES
('Eleanor', 'Vance', '1988-04-12', 'Female', 'O+', '+1 (555) 234-5678', 'eleanor.vance@example.com', '742 Evergreen Terrace, Springfield', 'Thomas Vance (Spouse)', '+1 (555) 234-5679', 'Penicillin, Shellfish', 'Mild asthma, seasonal pollen allergy'),
('Marcus', 'Sterling', '1975-09-23', 'Male', 'A-', '+1 (555) 345-6789', 'marcus.sterling@example.com', '124 Conch Street, Pacific Grove', 'Clara Sterling (Sister)', '+1 (555) 345-6780', 'Sulfa drugs', 'Hypertension diagnosed in 2021'),
('Sophia', 'Chen', '1995-11-05', 'Female', 'B+', '+1 (555) 456-7890', 'sophia.chen@example.com', '88 Blossom Hill Rd, San Jose', 'David Chen (Father)', '+1 (555) 456-7891', 'None known', 'No major prior conditions'),
('Arthur', 'Pendleton', '1962-02-18', 'Male', 'AB+', '+1 (555) 567-8901', 'arthur.p@example.com', '405 Elm Boulevard, Riverdale', 'Martha Pendleton (Spouse)', '+1 (555) 567-8902', 'Aspirin, NSAIDs', 'Type 2 Diabetes, Coronary artery stent (2019)'),
('Aaliyah', 'Patel', '2001-07-30', 'Female', 'O-', '+1 (555) 678-9012', 'aaliyah.patel@example.com', '19 Oakwood Drive, Austin', 'Sunita Patel (Mother)', '+1 (555) 678-9013', 'Latex, Peanuts', 'Chronic migraine headaches');

-- 2. Insert Sample Doctors
INSERT INTO doctors (first_name, last_name, specialization, department, qualification, experience_years, consulting_fee, phone, email, available_days, available_hours, status) VALUES
('Dr. Alexander', 'Wright', 'Cardiologist', 'Cardiology', 'MD, FACC, Harvard Medical', 16, 150.00, '+1 (555) 101-2001', 'dr.wright@hms-hospital.org', 'Mon, Wed, Fri', '09:00 AM - 04:00 PM', 'ACTIVE'),
('Dr. Beatrice', 'Morgan', 'Neurologist', 'Neurology', 'MD, PhD, Johns Hopkins', 12, 180.00, '+1 (555) 101-2002', 'dr.morgan@hms-hospital.org', 'Tue, Thu, Sat', '10:00 AM - 05:00 PM', 'ACTIVE'),
('Dr. Christopher', 'Lee', 'General Physician', 'Internal Medicine', 'MBBS, MD, Stanford Medicine', 10, 80.00, '+1 (555) 101-2003', 'dr.lee@hms-hospital.org', 'Mon, Tue, Wed, Thu, Fri', '08:30 AM - 04:30 PM', 'ACTIVE'),
('Dr. Diana', 'Rossi', 'Orthopedic Surgeon', 'Orthopedics', 'MS (Ortho), Mayo Clinic', 14, 160.00, '+1 (555) 101-2004', 'dr.rossi@hms-hospital.org', 'Mon, Wed, Fri', '09:00 AM - 03:00 PM', 'ACTIVE'),
('Dr. Evelyn', 'Harper', 'Dermatologist', 'Dermatology', 'MD (Derm), Columbia Univ', 9, 110.00, '+1 (555) 101-2005', 'dr.harper@hms-hospital.org', 'Tue, Wed, Thu', '09:30 AM - 05:00 PM', 'ACTIVE');

-- 3. Insert Diagnostic Services Catalog
INSERT INTO diagnostic_services (service_name, category, cost, turnaround_hours, description) VALUES
('Complete Blood Count (CBC)', 'Pathology', 35.00, 6, 'Measures red blood cells, white blood cells, platelets, and hemoglobin.'),
('Comprehensive Metabolic Panel (CMP)', 'Biochemistry', 55.00, 8, 'Evaluates kidney function, liver function, blood glucose, and electrolytes.'),
('Lipid Panel & Cholesterol Profile', 'Biochemistry', 45.00, 12, 'Measures Total Cholesterol, HDL, LDL, and Triglycerides.'),
('12-Lead Electrocardiogram (ECG)', 'Cardiology', 75.00, 2, 'Records electrical signals of the heart to detect arrhythmias or ischemia.'),
('Digital Chest X-Ray (PA View)', 'Radiology', 90.00, 4, 'High resolution radiographic imaging of chest cavity, lungs, and heart size.'),
('Magnetic Resonance Imaging (MRI) Brain', 'Radiology', 450.00, 24, 'Detailed cross-sectional structural imaging of brain parenchyma.'),
('Glycated Hemoglobin (HbA1c)', 'Endocrinology', 40.00, 6, 'Assesses average blood sugar levels over the past 3-month window.'),
('Thyroid Stimulating Hormone (TSH)', 'Endocrinology', 50.00, 12, 'Evaluates thyroid gland activity and metabolic control.');

-- 4. Insert Appointments
INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, reason_for_visit, status, notes) VALUES
(1, 3, CURRENT_DATE, '09:00 AM', 'Routine checkup and mild shortness of breath during workouts', 'CONFIRMED', 'Patient requested morning slot.'),
(2, 1, CURRENT_DATE, '10:30 AM', 'Follow-up on elevated blood pressure and palpitations', 'SCHEDULED', 'Review recent home BP log.'),
(3, 5, CURRENT_DATE, '02:00 PM', 'Skin rash consultation and eczema evaluation', 'CONFIRMED', 'Patient brought pictures of allergic flare-up.'),
(4, 1, DATEADD('DAY', 1, CURRENT_DATE), '11:00 AM', 'Cardiac post-stent annual review & echocardiogram referral', 'SCHEDULED', 'Prior records on file.'),
(5, 2, DATEADD('DAY', 2, CURRENT_DATE), '03:00 PM', 'Recurrent severe temporal headaches with aura', 'SCHEDULED', 'Headache journal provided by patient.'),
(2, 3, DATEADD('DAY', -3, CURRENT_DATE), '11:30 AM', 'Flu symptoms and persistent dry cough', 'COMPLETED', 'Prescribed antibiotics and cough suppressant.');

-- 5. Insert Medical Records
INSERT INTO medical_records (patient_id, doctor_id, appointment_id, record_date, diagnosis, symptoms, blood_pressure, heart_rate, temperature, weight_kg, treatment_plan, doctor_notes) VALUES
(2, 3, 6, DATEADD('DAY', -3, CURRENT_DATE), 'Acute Bronchitis & Viral Upper Respiratory Infection', 'Cough, low grade fever, mild chest discomfort', '135/88', 82, 38.1, 78.5, '5-day course of Azithromycin, rest, hydration, and steam inhalation.', 'Lungs showed mild rhonchi bilaterally. Advised patient to rest and return if breathing difficulty worsens.'),
(4, 1, NULL, DATEADD('DAY', -14, CURRENT_DATE), 'Essential Hypertension & Stable Angina', 'Exertional chest tightness, fatigue', '142/90', 74, 36.8, 84.0, 'Continue Amlodipine and Atorvastatin, low-sodium diet, regular aerobic walking.', 'Patient shows good adherence to medications. Scheduled diagnostic lipid check.');

-- 6. Insert Prescriptions
INSERT INTO prescriptions (record_id, medicine_name, dosage, frequency, duration, instructions) VALUES
(1, 'Azithromycin', '500 mg', 'Once daily', '5 days', 'Take 1 hour before or 2 hours after meals with a full glass of water'),
(1, 'Levocetirizine', '5 mg', 'Once daily at bedtime', '7 days', 'May cause drowsiness'),
(2, 'Amlodipine Besylate', '5 mg', 'Once daily in the morning', '30 days', 'Monitor BP twice weekly'),
(2, 'Atorvastatin Calcium', '20 mg', 'Once daily at night', '30 days', 'Avoid grapefruit consumption');

-- 7. Insert Diagnostic Tests
INSERT INTO diagnostic_tests (patient_id, service_id, doctor_id, appointment_id, booking_date, status, results, normal_range, remarks, completed_at) VALUES
(2, 1, 3, 6, DATEADD('DAY', -3, CURRENT_DATE), 'COMPLETED', 'WBC: 11,200 /uL (Mild leukocytosis), RBC: 4.8 M/uL, Platelets: 240,000 /uL, Hemoglobin: 14.8 g/dL', 'WBC: 4,500 - 11,000 /uL', 'Consistent with acute inflammatory or mild infectious response.', CURRENT_TIMESTAMP),
(4, 3, 1, NULL, DATEADD('DAY', -14, CURRENT_DATE), 'COMPLETED', 'Total Cholesterol: 195 mg/dL, HDL: 46 mg/dL, LDL: 118 mg/dL, Triglycerides: 155 mg/dL', 'Total Chol: < 200 mg/dL, LDL: < 100 mg/dL', 'Target LDL reached within borderline control. Continue lipid-lowering therapy.', CURRENT_TIMESTAMP),
(1, 5, 3, 1, CURRENT_DATE, 'ORDERED', NULL, 'Normal lung field clarity', 'Chest X-Ray requisitioned to rule out pulmonary congestion.', NULL),
(5, 6, 2, 5, DATEADD('DAY', 2, CURRENT_DATE), 'ORDERED', NULL, 'Brain parenchyma structural integrity', 'Rule out vascular anomalies for migraine with aura.', NULL);

-- 8. Insert Invoices
INSERT INTO invoices (patient_id, appointment_id, invoice_number, invoice_date, subtotal, tax, discount, total_amount, payment_status, payment_method, paid_amount, notes) VALUES
(2, 6, 'INV-2026-0001', DATEADD('DAY', -3, CURRENT_DATE), 115.00, 5.75, 0.00, 120.75, 'PAID', 'Credit Card', 120.75, 'Consultation fee ($80) + CBC lab test ($35)'),
(4, NULL, 'INV-2026-0002', DATEADD('DAY', -14, CURRENT_DATE), 195.00, 9.75, 10.00, 194.75, 'PAID', 'Health Insurance', 194.75, 'Cardiology consultation ($150) + Lipid profile ($45) with senior citizen discount ($10)'),
(1, 1, 'INV-2026-0003', CURRENT_DATE, 170.00, 8.50, 0.00, 178.50, 'UNPAID', 'Pending', 0.00, 'Physician consultation ($80) + Digital Chest X-Ray ($90)');

-- 9. Insert Invoice Items
INSERT INTO invoice_items (invoice_id, description, item_type, quantity, unit_price, total_price) VALUES
(1, 'General Physician Consultation (Dr. Christopher Lee)', 'CONSULTATION', 1, 80.00, 80.00),
(1, 'Complete Blood Count (CBC) Diagnostic Test', 'DIAGNOSTIC', 1, 35.00, 35.00),
(2, 'Cardiology Specialist Consultation (Dr. Alexander Wright)', 'CONSULTATION', 1, 150.00, 150.00),
(2, 'Lipid Panel & Cholesterol Profile', 'DIAGNOSTIC', 1, 45.00, 45.00),
(3, 'General Physician Consultation (Dr. Christopher Lee)', 'CONSULTATION', 1, 80.00, 80.00),
(3, 'Digital Chest X-Ray (PA View)', 'DIAGNOSTIC', 1, 90.00, 90.00);

-- 10. Insert Audit Logs / Edit-History (Demonstrating Data Traceability)
INSERT INTO audit_logs (entity_type, entity_id, action_type, field_name, old_value, new_value, modified_by, reason_or_notes) VALUES
('PATIENT', 1, 'CREATE', 'ALL_FIELDS', NULL, 'Registered patient Eleanor Vance', 'Nurse J. Adams', 'Initial patient intake registration'),
('PATIENT', 1, 'UPDATE', 'allergies', 'Penicillin', 'Penicillin, Shellfish', 'Dr. Christopher Lee', 'Patient reported recent allergic reaction to shellfish'),
('PATIENT', 2, 'UPDATE', 'phone', '+1 (555) 345-0000', '+1 (555) 345-6789', 'Receptionist K. Miller', 'Updated primary mobile contact per patient request'),
('APPOINTMENT', 1, 'STATUS_CHANGE', 'status', 'SCHEDULED', 'CONFIRMED', 'Frontdesk System', 'Patient confirmed appointment via phone call'),
('INVOICE', 1, 'STATUS_CHANGE', 'payment_status', 'UNPAID', 'PAID', 'Billing Clerk R. Gomez', 'Full payment processed via Terminal Card Reader');
