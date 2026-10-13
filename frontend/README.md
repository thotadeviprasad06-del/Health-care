# Health Management System - Frontend

This directory contains the user interface and client logic for the Health Management System.

## Structure

```
frontend/
├── index.html           # Main semantic single-page layout & modal dialogues
├── css/
│   └── styles.css       # Vanilla CSS clinical design system (Teal & Slate theme, glassmorphism, print stylesheets)
└── js/
    └── app.js           # Client-side state management, REST API integration, toast notifications, conflict validation UI
```

## Features
- **Executive Dashboard**: Real-time KPI statistics, recent consultations, and audit activity.
- **Patient Management**: Structured patient intake registration, search/filter, and profile modal with **Edit-History** timeline.
- **Doctor Directory**: Specialist cards with shift hours, fees, and department badges.
- **Appointment Scheduler**: Dynamic time-slot selector with instant schedule conflict alerts.
- **Medical Records (EMR)**: Vital signs recording and multi-item prescription generator.
- **Diagnostic Laboratory**: Catalog inspection, specimen tracking, and diagnostic findings entry.
- **Billing & Invoices**: Itemized invoice builder, payment recording, and printable official hospital tax receipts.
- **Audit Trail**: Complete data traceability table showing field-level changes.
