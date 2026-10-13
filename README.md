# Health Management System (HMS)

A modern, web-based healthcare management platform designed to simplify, digitize, and automate healthcare administrative and clinical activities. Built using **Java 21**, **JDBC**, **MySQL / Embedded DB**, **HTML5**, **Vanilla CSS3**, and **JavaScript**.

---

## Key Modules & Features

1. **Patient Management & Structured Registration**:
   - Structured registration capturing personal demographics, contact info, blood group, emergency contacts, known allergies, and medical history.
   - Real-time search and filterable directory.
   - Patient Profile view with an integrated **Edit-History** timeline.

2. **Data Traceability & Edit-History (Audit Trail)**:
   - Maintains a tamper-resistant record of every modification made to patient data and critical clinical records.
   - Captures entity ID, field modified, previous value (`old_value`), new value (`new_value`), modifying staff member (`modified_by`), timestamp, and change rationale.

3. **Doctor Rostering & Profiles**:
   - Specialist roster covering Cardiology, Neurology, Internal Medicine, Orthopedics, and Dermatology.
   - Consulting fees, shift hours, and available days.

4. **Conflict-Free Appointment Scheduling**:
   - Dynamic time slot booking (09:00 AM to 05:00 PM).
   - Real-time conflict-detection engine prevents double-booking doctors for the same slot.
   - Status workflow management: `Scheduled` &rarr; `Confirmed` &rarr; `Completed` &rarr; `Cancelled`.

5. **Electronic Medical Records (EMR) & Prescriptions**:
   - Clinical encounters, diagnoses, presenting symptoms, and vitals tracking (Blood Pressure, Heart Rate, Temperature, Weight).
   - Multi-medication prescription builder with dosage, frequency, and duration.

6. **Diagnostic Services & Laboratory**:
   - Test catalog (CBC, Lipid Panel, ECG, MRI Brain, Chest X-Ray, etc.).
   - Lab test orders, sample tracking (`Ordered`, `Sample Collected`, `In Progress`, `Completed`), reference normal ranges, and verified pathologist findings.

7. **Itemized Billing & Printable Receipts**:
   - Automated itemized invoices combining doctor consultation fees, diagnostic lab tests, and procedures.
   - Healthcare tax calculation and discount application.
   - Payment processing (`Unpaid`, `Partially Paid`, `Paid`) with payment method logging.
   - Hospital tax invoice view formatted for printing and PDF export.

8. **Executive Analytics Dashboard**:
   - Real-time KPIs (Total Patients, Active Doctors, Today's Appointments, Pending Lab Tests, Total Revenue Collected).
   - Recent activity feed and instant audit stream.

---

## Directory Organization

The project is cleanly divided into dedicated `frontend` and `backend` modules:

```
health care/
├── frontend/                     # Modern Clinical User Interface
│   ├── index.html                # Semantic HTML5 layout & modal dialogues
│   ├── css/
│   │   └── styles.css            # Vanilla CSS design system (Teal & Slate theme)
│   ├── js/
│   │   └── app.js                # Client SPA logic, API fetch calls & validation
│   └── README.md
│
├── backend/                      # Java 21 & Pure JDBC Backend
│   ├── pom.xml                   # Maven dependencies (Java 21, Spring Boot, MySQL, H2)
│   ├── setup-mysql.sql           # MySQL 8.0 schema and seed dataset
│   ├── src/main/java/            # Java Models, DAOs, Services, Controllers
│   ├── src/main/resources/       # application.properties, schema.sql, data.sql
│   └── README.md
│
├── setup-mysql.sql               # Root MySQL 8.0 setup script
└── README.md                     # Root project documentation
```

---

## Running the Application

### Prerequisites
- Java JDK 21
- Apache Maven 3.8+

### Quick Start (Embedded Mode)
The system is configured to launch out-of-the-box on port `8085` without requiring manual database installation:

```bash
mvn package -DskipTests
java -jar target/health-management-system-1.0.0.jar
```
Or run directly using Maven:
```bash
mvn spring-boot:run
```

Open your browser and navigate to:
```
http://localhost:8085
```

---

### Connecting to Local MySQL 8.0 Server

To use your local MySQL 8.0 server instead:

1. Import the database schema and sample records:
   ```bash
   mysql -u root -p < setup-mysql.sql
   ```

2. Open `src/main/resources/application.properties` and uncomment the MySQL lines:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/health_management_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
   spring.datasource.username=root
   spring.datasource.password=YOUR_MYSQL_PASSWORD
   ```

3. Restart the application.

---

## REST API Summary

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/analytics/dashboard` | Dashboard KPIs and summary statistics |
| `GET` | `/api/patients` | List patients (supports `?search=...`) |
| `POST` | `/api/patients` | Register new patient (creates audit log) |
| `PUT` | `/api/patients/{id}` | Update patient (records field diffs into edit-history) |
| `DELETE` | `/api/patients/{id}` | Delete patient record |
| `GET` | `/api/doctors` | List doctors in roster |
| `GET` | `/api/appointments` | List appointments (filterable by date, doctor, status) |
| `POST` | `/api/appointments` | Book appointment (enforces conflict check) |
| `PATCH`| `/api/appointments/{id}/status` | Update appointment status |
| `GET` | `/api/medical-records` | List medical records & prescriptions |
| `POST` | `/api/medical-records` | Create medical record encounter |
| `GET` | `/api/diagnostics/services` | Diagnostic catalog tests |
| `GET` | `/api/diagnostics/tests` | List booked diagnostic tests |
| `PATCH`| `/api/diagnostics/tests/{id}/results`| Record test results & verify |
| `GET` | `/api/billing/invoices` | List invoices |
| `POST` | `/api/billing/invoices` | Generate itemized invoice |
| `POST` | `/api/billing/invoices/{id}/payment` | Record payment |
| `GET` | `/api/audit-logs` | Retrieve system audit logs |
| `GET` | `/api/audit-logs/{entityType}/{id}` | Retrieve edit history for specific record |
