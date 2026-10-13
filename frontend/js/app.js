/**
 * Health Management System (HMS) - Core Web Client Application
 */

// Global State
const state = {
  currentView: 'dashboard',
  currentRole: 'Healthcare Staff',
  patients: [],
  doctors: [],
  catalogServices: [],
  appointments: [],
  medicalRecords: [],
  diagnosticTests: [],
  invoices: [],
  auditLogs: []
};

// Initialize Application on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  initClock();
  initNavigation();
  initRoleSelector();
  initQuickAction();
  initBackendConfigUI();
  
  // Initial data load
  loadDashboard();
  loadPatients();
  loadDoctors();
  loadDiagnosticServices();
});

// Live Clock in Topbar
function initClock() {
  const clockEl = document.getElementById('liveClock');
  const update = () => {
    const now = new Date();
    clockEl.textContent = now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }) + 
                          ' • ' + now.toLocaleDateString([], { month: 'short', day: 'numeric', year: 'numeric' });
  };
  update();
  setInterval(update, 1000);
}

// Role Selector
function initRoleSelector() {
  const roleSelect = document.getElementById('activeRole');
  state.currentRole = roleSelect.value;
  roleSelect.addEventListener('change', (e) => {
    state.currentRole = e.target.value;
    showToast(`Active role switched to: ${state.currentRole}`, 'info');
  });
}

// Quick Action CTA Button
function initQuickAction() {
  const btn = document.getElementById('btnQuickAction');
  btn.addEventListener('click', () => {
    switch(state.currentView) {
      case 'dashboard':
      case 'patients':
        openPatientModal();
        break;
      case 'doctors':
      case 'appointments':
        openAppointmentModal();
        break;
      case 'records':
        openRecordModal();
        break;
      case 'diagnostics':
        openDiagnosticModal();
        break;
      case 'billing':
        openInvoiceModal();
        break;
      default:
        openPatientModal();
    }
  });
}

// Navigation Handling
function initNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const view = item.getAttribute('data-view');
      if (view) navigateTo(view);
    });
  });
}

function navigateTo(viewName) {
  state.currentView = viewName;

  // Update Nav Menu UI
  document.querySelectorAll('.nav-item').forEach(el => el.classList.remove('active'));
  const activeNav = document.getElementById(`nav-${viewName}`);
  if (activeNav) activeNav.classList.add('active');

  // Update Content Views
  document.querySelectorAll('.view-section').forEach(el => el.classList.remove('active'));
  const targetView = document.getElementById(`view-${viewName}`);
  if (targetView) targetView.classList.add('active');

  // Update Topbar Title & Quick Action CTA
  const titleEl = document.getElementById('top-view-title');
  const descEl = document.getElementById('top-view-desc');
  const ctaLabel = document.getElementById('quickActionLabel');

  switch (viewName) {
    case 'dashboard':
      titleEl.textContent = 'Executive Dashboard';
      descEl.textContent = 'Centralized overview of patient intake, consultations, lab tests & billing.';
      ctaLabel.textContent = 'New Patient Intake';
      loadDashboard();
      break;
    case 'patients':
      titleEl.textContent = 'Patient Management';
      descEl.textContent = 'Maintain patient demographics, contact details, medical history, allergies, and change traceability.';
      ctaLabel.textContent = 'Register Patient';
      loadPatients();
      break;
    case 'doctors':
      titleEl.textContent = 'Doctor Directory & Rostering';
      descEl.textContent = 'Specialist roster, consultation fees, scheduled shifts, and real-time department availability.';
      ctaLabel.textContent = 'Book Consultation';
      loadDoctors();
      break;
    case 'appointments':
      titleEl.textContent = 'Appointments & Slot Scheduling';
      descEl.textContent = 'Schedule conflict-free consultations, check available physician time slots, and track visit status.';
      ctaLabel.textContent = 'Book Appointment';
      loadAppointments();
      break;
    case 'records':
      titleEl.textContent = 'Medical Records (EMR) & Prescriptions';
      descEl.textContent = 'Document patient clinical diagnoses, vital statistics (BP, pulse, temp, weight), and itemized prescriptions.';
      ctaLabel.textContent = 'Create Medical Record';
      loadMedicalRecords();
      break;
    case 'diagnostics':
      titleEl.textContent = 'Diagnostic Services & Laboratory';
      descEl.textContent = 'Manage test bookings, specimen statuses, normal reference ranges, and verified lab results.';
      ctaLabel.textContent = 'Order Lab Test';
      loadDiagnosticTests();
      break;
    case 'billing':
      titleEl.textContent = 'Billing & Invoices';
      descEl.textContent = 'Generate unified hospital invoices combining consultations, diagnostic tests, and clinical procedures.';
      ctaLabel.textContent = 'Generate Invoice';
      loadBillingInvoices();
      break;
    case 'audit':
      titleEl.textContent = 'Edit-History & Data Traceability';
      descEl.textContent = 'Complete regulatory audit trail tracking who changed what, previous values, new values, and timestamps.';
      ctaLabel.textContent = 'New Patient Intake';
      loadAuditLogs();
      break;
  }
}

// Dynamic Backend API Host determination (Supports Vercel -> Render integration)
function getApiUrl(endpoint) {
  // 1. Injected via window (e.g. from js/config.js)
  if (typeof window !== 'undefined' && window.BACKEND_API_URL && window.BACKEND_API_URL.trim() !== '') {
    const base = window.BACKEND_API_URL.trim().replace(/\/+$/, '');
    return endpoint.startsWith('http') ? endpoint : `${base}${endpoint}`;
  }
  // 2. Saved via user settings in localStorage
  if (typeof localStorage !== 'undefined') {
    const saved = localStorage.getItem('HMS_BACKEND_URL');
    if (saved && saved.trim() !== '') {
      const base = saved.trim().replace(/\/+$/, '');
      return endpoint.startsWith('http') ? endpoint : `${base}${endpoint}`;
    }
  }
  // 3. Fallback: relative path (works on localhost or via Vercel rewrites)
  return endpoint;
}

// Global API Fetch Helper with Role Header
async function apiFetch(url, options = {}) {
  const headers = options.headers || {};
  headers['Content-Type'] = 'application/json';
  headers['X-User-Role'] = state.currentRole;
  options.headers = headers;
  
  const targetUrl = getApiUrl(url);
  const response = await fetch(targetUrl, options);
  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || `Request failed with status ${response.status}`);
  }
  return response.json();
}

// ==============================================================================
// 1. DASHBOARD MODULE
// ==============================================================================
async function loadDashboard() {
  try {
    const stats = await apiFetch('/api/analytics/dashboard');
    
    document.getElementById('stat-total-patients').textContent = stats.totalPatients || 0;
    document.getElementById('stat-active-doctors').textContent = stats.totalDoctors || 0;
    document.getElementById('stat-today-appointments').textContent = stats.todayAppointments || 0;
    document.getElementById('stat-pending-tests').textContent = stats.pendingLabTests || 0;
    document.getElementById('stat-total-revenue').textContent = formatCurrency(stats.totalRevenue || 0);
    
    document.getElementById('badge-patients-count').textContent = stats.totalPatients || 0;

    // Render Recent Appointments table
    renderDashboardAppointments(stats.recentAppointments || []);
    
    // Render Dashboard Audit feed
    renderDashboardAuditFeed(stats.recentAuditLogs || []);
  } catch (err) {
    console.error('Error loading dashboard:', err);
  }
}

function renderDashboardAppointments(appointments) {
  const tbody = document.getElementById('dashboard-appointments-body');
  if (!appointments || appointments.length === 0) {
    tbody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:var(--text-muted); padding:20px;">No recent appointments found</td></tr>`;
    return;
  }

  tbody.innerHTML = appointments.map(a => `
    <tr>
      <td>
        <div class="avatar-cell">
          <div class="avatar-circle">${getInitials(a.patientName)}</div>
          <div class="avatar-info">
            <h4>${escapeHtml(a.patientName || 'Unknown')}</h4>
            <p>${escapeHtml(a.patientPhone || '')}</p>
          </div>
        </div>
      </td>
      <td>
        <strong>${escapeHtml(a.doctorName || 'Dr.')}</strong><br>
        <span class="badge badge-dept">${escapeHtml(a.doctorDepartment || '')}</span>
      </td>
      <td>
        <strong>${escapeHtml(a.appointmentTime)}</strong><br>
        <span style="font-size:11px; color:var(--text-muted);">${escapeHtml(a.appointmentDate)}</span>
      </td>
      <td>${getStatusBadge(a.status)}</td>
      <td>
        <button class="btn-icon" title="View in Appointments" onclick="navigateTo('appointments')">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"/><polyline points="15 3 21 3 21 9"/><line x1="10" y1="14" x2="21" y2="3"/></svg>
        </button>
      </td>
    </tr>
  `).join('');
}

function renderDashboardAuditFeed(logs) {
  const feed = document.getElementById('dashboard-audit-feed');
  if (!logs || logs.length === 0) {
    feed.innerHTML = `<div style="text-align:center; color:var(--text-muted); padding:20px;">No recent changes logged</div>`;
    return;
  }

  feed.innerHTML = logs.map(l => `
    <div class="timeline-item">
      <div class="timeline-node"></div>
      <div class="timeline-card">
        <div class="timeline-head">
          <h5>${escapeHtml(l.actionType)}: ${escapeHtml(l.entityType)} #${escapeHtml(l.entityId)}</h5>
          <span class="timeline-time">${formatDate(l.timestamp)}</span>
        </div>
        <div class="timeline-body">
          ${escapeHtml(l.reasonOrNotes || '')}
          ${l.oldValue || l.newValue ? `
            <div class="diff-box">
              ${l.oldValue ? `<span class="diff-old">${escapeHtml(l.oldValue)}</span> &rarr;` : ''}
              <span class="diff-new">${escapeHtml(l.newValue || '')}</span>
            </div>
          ` : ''}
          <div style="font-size:11px; color:var(--text-light); margin-top:4px;">By: ${escapeHtml(l.modifiedBy || 'System')}</div>
        </div>
      </div>
    </div>
  `).join('');
}

// ==============================================================================
// 2. PATIENTS MODULE
// ==============================================================================
let patientSearchTimeout = null;
function handlePatientSearch() {
  clearTimeout(patientSearchTimeout);
  patientSearchTimeout = setTimeout(() => {
    const q = document.getElementById('patientSearchInput').value;
    loadPatients(q);
  }, 300);
}

async function loadPatients(search = '') {
  try {
    const url = search ? `/api/patients?search=${encodeURIComponent(search)}` : '/api/patients';
    const patients = await apiFetch(url);
    state.patients = patients;
    renderPatientsTable(patients);
    document.getElementById('badge-patients-count').textContent = patients.length;
  } catch (err) {
    showToast(`Failed to load patients: ${err.message}`, 'danger');
  }
}

function renderPatientsTable(patients) {
  const tbody = document.getElementById('patients-table-body');
  if (!patients || patients.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:24px;">No patients found</td></tr>`;
    return;
  }

  tbody.innerHTML = patients.map(p => `
    <tr>
      <td><strong>#${p.patientId}</strong></td>
      <td>
        <div class="avatar-cell">
          <div class="avatar-circle">${getInitials(p.fullName)}</div>
          <div class="avatar-info">
            <h4>${escapeHtml(p.fullName)}</h4>
            <p>${escapeHtml(p.address || 'Address not listed')}</p>
          </div>
        </div>
      </td>
      <td>
        <strong>${escapeHtml(p.dob || '')}</strong><br>
        <span style="font-size:12px; color:var(--text-muted);">${escapeHtml(p.gender)}</span>
      </td>
      <td>
        <span class="badge badge-blood">${escapeHtml(p.bloodGroup || 'N/A')}</span>
      </td>
      <td>
        <strong>${escapeHtml(p.phone)}</strong><br>
        <span style="font-size:12px; color:var(--text-muted);">${escapeHtml(p.email || 'No email')}</span>
      </td>
      <td>
        <div style="max-width:220px; font-size:12.5px;">
          ${p.allergies ? `<span style="color:#b91c1c; font-weight:600;">Allergies:</span> ${escapeHtml(p.allergies)}<br>` : ''}
          ${p.medicalHistory ? `<span style="color:var(--text-muted);">History: ${escapeHtml(p.medicalHistory)}</span>` : '<span style="color:var(--text-light);">None noted</span>'}
        </div>
      </td>
      <td>
        <div class="action-btns">
          <button class="btn-icon" title="View Profile & Edit-History" onclick="viewPatientProfile(${p.patientId})">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
          </button>
          <button class="btn-icon" title="Edit Patient Details" onclick="editPatient(${p.patientId})">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>
          </button>
          <button class="btn-icon btn-danger" title="Delete Patient" onclick="deletePatient(${p.patientId})">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
          </button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openPatientModal(patient = null) {
  const form = document.getElementById('form-patient');
  form.reset();
  const reasonGroup = document.getElementById('edit-reason-group');
  const title = document.getElementById('modalPatientTitle');

  if (patient) {
    title.textContent = `Edit Patient Profile (#${patient.patientId})`;
    document.getElementById('patient-id').value = patient.patientId;
    document.getElementById('patient-first-name').value = patient.firstName;
    document.getElementById('patient-last-name').value = patient.lastName;
    document.getElementById('patient-dob').value = patient.dob;
    document.getElementById('patient-gender').value = patient.gender;
    document.getElementById('patient-blood-group').value = patient.bloodGroup || 'O+';
    document.getElementById('patient-phone').value = patient.phone;
    document.getElementById('patient-email').value = patient.email || '';
    document.getElementById('patient-emergency-name').value = patient.emergencyContactName || '';
    document.getElementById('patient-emergency-phone').value = patient.emergencyContactPhone || '';
    document.getElementById('patient-address').value = patient.address || '';
    document.getElementById('patient-allergies').value = patient.allergies || '';
    document.getElementById('patient-medical-history').value = patient.medicalHistory || '';
    
    // Show audit reason requirement
    reasonGroup.style.display = 'block';
    document.getElementById('patient-edit-reason').required = true;
  } else {
    title.textContent = 'Register New Patient';
    document.getElementById('patient-id').value = '';
    reasonGroup.style.display = 'none';
    document.getElementById('patient-edit-reason').required = false;
  }

  openModal('modal-patient');
}

async function editPatient(patientId) {
  try {
    const patient = await apiFetch(`/api/patients/${patientId}`);
    openPatientModal(patient);
  } catch (err) {
    showToast(`Failed to fetch patient details: ${err.message}`, 'danger');
  }
}

async function handleSavePatient(e) {
  e.preventDefault();
  const id = document.getElementById('patient-id').value;
  const payload = {
    firstName: document.getElementById('patient-first-name').value.trim(),
    lastName: document.getElementById('patient-last-name').value.trim(),
    dob: document.getElementById('patient-dob').value,
    gender: document.getElementById('patient-gender').value,
    bloodGroup: document.getElementById('patient-blood-group').value,
    phone: document.getElementById('patient-phone').value.trim(),
    email: document.getElementById('patient-email').value.trim(),
    emergencyContactName: document.getElementById('patient-emergency-name').value.trim(),
    emergencyContactPhone: document.getElementById('patient-emergency-phone').value.trim(),
    address: document.getElementById('patient-address').value.trim(),
    allergies: document.getElementById('patient-allergies').value.trim(),
    medicalHistory: document.getElementById('patient-medical-history').value.trim()
  };

  try {
    if (id) {
      const reason = document.getElementById('patient-edit-reason').value.trim();
      await apiFetch(`/api/patients/${id}?reason=${encodeURIComponent(reason)}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      });
      showToast('Patient updated & change logged to audit trail!', 'success');
    } else {
      await apiFetch('/api/patients', {
        method: 'POST',
        body: JSON.stringify(payload)
      });
      showToast('New patient registered successfully!', 'success');
    }
    closeModal('modal-patient');
    loadPatients();
    loadDashboard();
  } catch (err) {
    showToast(`Error saving patient: ${err.message}`, 'danger');
  }
}

async function deletePatient(patientId) {
  if (!confirm(`Are you sure you want to delete patient #${patientId}? This action will be audited.`)) {
    return;
  }

  try {
    await apiFetch(`/api/patients/${patientId}`, { method: 'DELETE' });
    showToast(`Patient #${patientId} record removed & logged to audit.`, 'warning');
    loadPatients();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to delete patient: ${err.message}`, 'danger');
  }
}

// View Patient Profile & Traceability History Modal
async function viewPatientProfile(patientId) {
  try {
    const patient = await apiFetch(`/api/patients/${patientId}`);
    const auditLogs = await apiFetch(`/api/audit-logs/PATIENT/${patientId}`);

    document.getElementById('profilePatientName').textContent = `${patient.fullName} (Patient #${patient.patientId})`;

    const detailsBox = document.getElementById('patient-profile-details');
    detailsBox.innerHTML = `
      <div style="background:var(--surface-subtle); padding:18px; border-radius:var(--radius-md); border:1px solid var(--border);">
        <div style="display:grid; grid-template-columns: repeat(3, 1fr); gap:12px; font-size:13.5px;">
          <div><span style="color:var(--text-muted); font-size:12px;">Date of Birth:</span><br><strong>${escapeHtml(patient.dob)}</strong></div>
          <div><span style="color:var(--text-muted); font-size:12px;">Gender / Blood:</span><br><strong>${escapeHtml(patient.gender)} / ${escapeHtml(patient.bloodGroup || 'N/A')}</strong></div>
          <div><span style="color:var(--text-muted); font-size:12px;">Contact Phone:</span><br><strong>${escapeHtml(patient.phone)}</strong></div>
          <div><span style="color:var(--text-muted); font-size:12px;">Email:</span><br><strong>${escapeHtml(patient.email || 'N/A')}</strong></div>
          <div><span style="color:var(--text-muted); font-size:12px;">Emergency Contact:</span><br><strong>${escapeHtml(patient.emergencyContactName || 'N/A')} (${escapeHtml(patient.emergencyContactPhone || '')})</strong></div>
          <div><span style="color:var(--text-muted); font-size:12px;">Address:</span><br><strong>${escapeHtml(patient.address || 'N/A')}</strong></div>
        </div>
        <div style="margin-top:14px; border-top:1px solid var(--border); padding-top:10px;">
          <div style="font-size:13px; margin-bottom:4px;"><strong style="color:#b91c1c;">Allergies:</strong> ${escapeHtml(patient.allergies || 'None recorded')}</div>
          <div style="font-size:13px;"><strong style="color:var(--text-muted);">Medical History:</strong> ${escapeHtml(patient.medicalHistory || 'None recorded')}</div>
        </div>
      </div>
    `;

    const timeline = document.getElementById('patient-audit-timeline');
    if (!auditLogs || auditLogs.length === 0) {
      timeline.innerHTML = `<div style="color:var(--text-muted); padding:12px;">No historical edits found for this patient.</div>`;
    } else {
      timeline.innerHTML = auditLogs.map(l => `
        <div class="timeline-item">
          <div class="timeline-node"></div>
          <div class="timeline-card">
            <div class="timeline-head">
              <h5>Action: ${escapeHtml(l.actionType)} on [${escapeHtml(l.fieldName || 'RECORD')}]</h5>
              <span class="timeline-time">${formatDate(l.timestamp)}</span>
            </div>
            <div class="timeline-body">
              <div>${escapeHtml(l.reasonOrNotes || '')}</div>
              ${l.oldValue || l.newValue ? `
                <div class="diff-box">
                  ${l.oldValue ? `<span class="diff-old">${escapeHtml(l.oldValue)}</span> &rarr;` : ''}
                  <span class="diff-new">${escapeHtml(l.newValue || '')}</span>
                </div>
              ` : ''}
              <div style="font-size:11px; color:var(--text-light); margin-top:4px;">Changed By: ${escapeHtml(l.modifiedBy || 'Healthcare Staff')}</div>
            </div>
          </div>
        </div>
      `).join('');
    }

    openModal('modal-patient-profile');
  } catch (err) {
    showToast(`Failed to load patient history: ${err.message}`, 'danger');
  }
}

// ==============================================================================
// 3. DOCTORS MODULE
// ==============================================================================
async function loadDoctors() {
  try {
    const doctors = await apiFetch('/api/doctors');
    state.doctors = doctors;
    renderDoctorCards(doctors);
    populateDoctorSelects(doctors);
  } catch (err) {
    showToast(`Failed to load doctors: ${err.message}`, 'danger');
  }
}

function renderDoctorCards(doctors) {
  const container = document.getElementById('doctor-cards-container');
  if (!doctors || doctors.length === 0) {
    container.innerHTML = `<div style="color:var(--text-muted); padding:20px;">No doctors listed in the roster.</div>`;
    return;
  }

  container.innerHTML = doctors.map(d => `
    <div class="doctor-card">
      <div class="doctor-card-top">
        <div class="doctor-avatar">${getInitials(d.fullName)}</div>
        <div class="doctor-meta">
          <h4>${escapeHtml(d.fullName)}</h4>
          <p>${escapeHtml(d.specialization)}</p>
          <span>${escapeHtml(d.qualification)} • ${d.experienceYears} Yrs Exp</span>
        </div>
      </div>
      <div class="doctor-schedule-box">
        <div>
          <span style="color:var(--text-muted);">Department:</span>
          <strong>${escapeHtml(d.department)}</strong>
        </div>
        <div>
          <span style="color:var(--text-muted);">Available Days:</span>
          <span>${escapeHtml(d.availableDays || 'Mon-Fri')}</span>
        </div>
        <div>
          <span style="color:var(--text-muted);">Shift Hours:</span>
          <span>${escapeHtml(d.availableHours || '09:00 AM - 05:00 PM')}</span>
        </div>
        <div>
          <span style="color:var(--text-muted);">Contact:</span>
          <span>${escapeHtml(d.phone)}</span>
        </div>
      </div>
      <div class="doctor-card-footer">
        <div class="fee-tag">
          $${d.consultingFee} <span>/ consult</span>
        </div>
        <button class="btn-primary" style="padding:6px 14px; font-size:12px;" onclick="bookDoctorSpecific(${d.doctorId})">
          Book Visit
        </button>
      </div>
    </div>
  `).join('');
}

function populateDoctorSelects(doctors) {
  // Appointment doctor select
  const apptDoc = document.getElementById('appt-doctor');
  if (apptDoc) {
    apptDoc.innerHTML = doctors.map(d => `<option value="${d.doctorId}">${d.fullName} (${d.specialization}) - $${d.consultingFee}</option>`).join('');
  }

  // Filter doctor select
  const filterDoc = document.getElementById('filterApptDoctor');
  if (filterDoc) {
    filterDoc.innerHTML = `<option value="">All Doctors</option>` + doctors.map(d => `<option value="${d.doctorId}">${d.fullName}</option>`).join('');
  }

  // Medical Record doctor select
  const recDoc = document.getElementById('record-doctor');
  if (recDoc) {
    recDoc.innerHTML = doctors.map(d => `<option value="${d.doctorId}">${d.fullName} (${d.specialization})</option>`).join('');
  }

  // Diagnostic referring doctor select
  const diagDoc = document.getElementById('diag-doctor');
  if (diagDoc) {
    diagDoc.innerHTML = `<option value="">-- None / Self-referral --</option>` + doctors.map(d => `<option value="${d.doctorId}">${d.fullName}</option>`).join('');
  }
}

function bookDoctorSpecific(doctorId) {
  openAppointmentModal();
  document.getElementById('appt-doctor').value = doctorId;
}

// ==============================================================================
// 4. APPOINTMENTS MODULE
// ==============================================================================
async function loadAppointments() {
  try {
    const doctorId = document.getElementById('filterApptDoctor').value;
    const status = document.getElementById('filterApptStatus').value;
    
    let url = `/api/appointments?status=${encodeURIComponent(status)}`;
    if (doctorId) url += `&doctorId=${encodeURIComponent(doctorId)}`;
    
    const appts = await apiFetch(url);
    state.appointments = appts;
    renderAppointmentsTable(appts);
  } catch (err) {
    showToast(`Failed to load appointments: ${err.message}`, 'danger');
  }
}

function renderAppointmentsTable(appts) {
  const tbody = document.getElementById('appointments-table-body');
  if (!appts || appts.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:24px;">No appointments found for selected filter</td></tr>`;
    return;
  }

  tbody.innerHTML = appts.map(a => `
    <tr>
      <td><strong>#${a.appointmentId}</strong></td>
      <td>
        <strong>${escapeHtml(a.patientName)}</strong><br>
        <span style="font-size:12px; color:var(--text-muted);">${escapeHtml(a.patientPhone || '')}</span>
      </td>
      <td>
        <strong>${escapeHtml(a.doctorName)}</strong><br>
        <span class="badge badge-dept">${escapeHtml(a.doctorDepartment || '')}</span>
      </td>
      <td>
        <strong>${escapeHtml(a.appointmentDate)}</strong><br>
        <span style="font-size:12.5px; font-weight:600; color:var(--primary);">${escapeHtml(a.appointmentTime)}</span>
      </td>
      <td>
        <span style="font-size:12.5px;">${escapeHtml(a.reasonForVisit || 'General checkup')}</span>
      </td>
      <td>${getStatusBadge(a.status)}</td>
      <td>
        <div class="action-btns">
          <select class="filter-select" style="padding:4px 8px; font-size:12px;" onchange="updateAppointmentStatus(${a.appointmentId}, this.value)">
            <option value="">Update Status...</option>
            <option value="CONFIRMED">Confirm</option>
            <option value="COMPLETED">Mark Completed</option>
            <option value="CANCELLED">Cancel</option>
          </select>
          <button class="btn-icon btn-danger" title="Cancel Appointment" onclick="cancelAppointment(${a.appointmentId})">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></svg>
          </button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openAppointmentModal() {
  const form = document.getElementById('form-appointment');
  form.reset();
  
  // Set default date to today
  document.getElementById('appt-date').value = new Date().toISOString().split('T')[0];
  
  // Populate patient select
  populatePatientDropdown('appt-patient');

  // Hide conflict alert
  document.getElementById('conflict-alert-box').style.display = 'none';

  openModal('modal-appointment');
}

async function handleBookAppointment(e) {
  e.preventDefault();
  const alertBox = document.getElementById('conflict-alert-box');
  const alertMsg = document.getElementById('conflict-alert-msg');
  alertBox.style.display = 'none';

  const payload = {
    patientId: parseInt(document.getElementById('appt-patient').value),
    doctorId: parseInt(document.getElementById('appt-doctor').value),
    appointmentDate: document.getElementById('appt-date').value,
    appointmentTime: document.getElementById('appt-time').value,
    reasonForVisit: document.getElementById('appt-reason').value.trim(),
    notes: document.getElementById('appt-notes').value.trim(),
    status: 'SCHEDULED'
  };

  try {
    await apiFetch('/api/appointments', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    showToast('Appointment reserved & scheduled successfully!', 'success');
    closeModal('modal-appointment');
    loadAppointments();
    loadDashboard();
  } catch (err) {
    // Conflict detection feedback!
    alertBox.style.display = 'flex';
    alertMsg.textContent = err.message;
    showToast(err.message, 'danger');
  }
}

async function updateAppointmentStatus(appointmentId, status) {
  if (!status) return;
  try {
    await apiFetch(`/api/appointments/${appointmentId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status: status })
    });
    showToast(`Appointment status updated to: ${status}`, 'success');
    loadAppointments();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to update status: ${err.message}`, 'danger');
  }
}

async function cancelAppointment(appointmentId) {
  if (!confirm(`Cancel appointment #${appointmentId}?`)) return;
  try {
    await apiFetch(`/api/appointments/${appointmentId}`, { method: 'DELETE' });
    showToast('Appointment cancelled.', 'warning');
    loadAppointments();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to cancel appointment: ${err.message}`, 'danger');
  }
}

// ==============================================================================
// 5. MEDICAL RECORDS (EMR) & PRESCRIPTIONS
// ==============================================================================
async function loadMedicalRecords() {
  try {
    const records = await apiFetch('/api/medical-records');
    state.medicalRecords = records;
    renderRecordsTable(records);
  } catch (err) {
    showToast(`Failed to load medical records: ${err.message}`, 'danger');
  }
}

function renderRecordsTable(records) {
  const tbody = document.getElementById('records-table-body');
  if (!records || records.length === 0) {
    tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:var(--text-muted); padding:24px;">No medical records on file</td></tr>`;
    return;
  }

  tbody.innerHTML = records.map(r => `
    <tr>
      <td><strong>#${r.recordId}</strong></td>
      <td><strong>${escapeHtml(r.patientName)}</strong></td>
      <td>
        <strong>${escapeHtml(r.doctorName)}</strong><br>
        <span style="font-size:11px; color:var(--text-muted);">${escapeHtml(r.doctorSpecialization || '')}</span>
      </td>
      <td>${escapeHtml(r.recordDate)}</td>
      <td>
        <strong style="color:var(--primary);">${escapeHtml(r.diagnosis)}</strong><br>
        <span style="font-size:12px; color:var(--text-muted);">${escapeHtml(r.symptoms || 'None listed')}</span>
      </td>
      <td>
        <div style="font-size:12px; line-height:1.4;">
          ${r.bloodPressure ? `BP: <strong>${escapeHtml(r.bloodPressure)}</strong> | ` : ''}
          ${r.heartRate ? `HR: <strong>${r.heartRate} bpm</strong><br>` : ''}
          ${r.temperature ? `Temp: <strong>${r.temperature}&deg;C</strong> | ` : ''}
          ${r.weightKg ? `Wt: <strong>${r.weightKg} kg</strong>` : ''}
        </div>
      </td>
      <td>
        <div style="max-width:240px; font-size:12px;">
          ${(r.prescriptions && r.prescriptions.length > 0) ? r.prescriptions.map(p => `
            <div style="margin-bottom:4px; padding:4px 8px; background:var(--surface-subtle); border-radius:4px;">
              <strong>${escapeHtml(p.medicineName)}</strong> (${escapeHtml(p.dosage)}) - ${escapeHtml(p.frequency)} [${escapeHtml(p.duration)}]
            </div>
          `).join('') : '<span style="color:var(--text-light);">No prescriptions</span>'}
        </div>
      </td>
    </tr>
  `).join('');
}

function openRecordModal() {
  const form = document.getElementById('form-record');
  form.reset();
  
  document.getElementById('record-date').value = new Date().toISOString().split('T')[0];
  populatePatientDropdown('record-patient');
  
  // Clear Rx container and add 1 default row
  const rxContainer = document.getElementById('prescription-items-container');
  rxContainer.innerHTML = '';
  addPrescriptionRow();

  openModal('modal-record');
}

function addPrescriptionRow() {
  const container = document.getElementById('prescription-items-container');
  const div = document.createElement('div');
  div.className = 'prescription-row';
  div.style.display = 'grid';
  div.style.gridTemplateColumns = '2fr 1fr 1.5fr 1fr 24px';
  div.style.gap = '8px';
  div.style.alignItems = 'center';

  div.innerHTML = `
    <input type="text" class="form-control rx-med" placeholder="Medication Name (e.g. Amoxicillin)" required>
    <input type="text" class="form-control rx-dose" placeholder="Dosage (500mg)" required>
    <input type="text" class="form-control rx-freq" placeholder="Frequency (Twice daily)" required>
    <input type="text" class="form-control rx-dur" placeholder="Duration (7 days)" required>
    <button type="button" class="btn-icon btn-danger" onclick="this.parentElement.remove()" title="Remove Medication">&times;</button>
  `;
  container.appendChild(div);
}

async function handleSaveRecord(e) {
  e.preventDefault();
  
  // Extract prescriptions
  const rxRows = document.querySelectorAll('.prescription-row');
  const prescriptions = [];
  rxRows.forEach(row => {
    const med = row.querySelector('.rx-med').value.trim();
    const dose = row.querySelector('.rx-dose').value.trim();
    const freq = row.querySelector('.rx-freq').value.trim();
    const dur = row.querySelector('.rx-dur').value.trim();
    if (med) {
      prescriptions.push({
        medicineName: med,
        dosage: dose,
        frequency: freq,
        duration: dur,
        instructions: ''
      });
    }
  });

  const payload = {
    patientId: parseInt(document.getElementById('record-patient').value),
    doctorId: parseInt(document.getElementById('record-doctor').value),
    recordDate: document.getElementById('record-date').value,
    diagnosis: document.getElementById('record-diagnosis').value.trim(),
    symptoms: document.getElementById('record-symptoms').value.trim(),
    bloodPressure: document.getElementById('record-bp').value.trim(),
    heartRate: document.getElementById('record-hr').value ? parseInt(document.getElementById('record-hr').value) : null,
    temperature: document.getElementById('record-temp').value ? parseFloat(document.getElementById('record-temp').value) : null,
    weightKg: document.getElementById('record-weight').value ? parseFloat(document.getElementById('record-weight').value) : null,
    treatmentPlan: document.getElementById('record-treatment').value.trim(),
    doctorNotes: document.getElementById('record-notes').value.trim(),
    prescriptions: prescriptions
  };

  try {
    await apiFetch('/api/medical-records', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    showToast('Clinical medical record & prescriptions saved!', 'success');
    closeModal('modal-record');
    loadMedicalRecords();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to save record: ${err.message}`, 'danger');
  }
}

// ==============================================================================
// 6. DIAGNOSTIC SERVICES MODULE
// ==============================================================================
async function loadDiagnosticServices() {
  try {
    const services = await apiFetch('/api/diagnostics/services');
    state.catalogServices = services;
    
    // Populate select
    const diagSelect = document.getElementById('diag-service');
    if (diagSelect) {
      diagSelect.innerHTML = services.map(s => `
        <option value="${s.serviceId}">[${s.category}] ${s.serviceName} - $${s.cost} (${s.turnaroundHours}h turnaround)</option>
      `).join('');
    }
  } catch (err) {
    console.error('Failed to load diagnostic catalog:', err);
  }
}

async function loadDiagnosticTests() {
  try {
    const status = document.getElementById('filterTestStatus').value;
    const tests = await apiFetch(`/api/diagnostics/tests?status=${encodeURIComponent(status)}`);
    state.diagnosticTests = tests;
    renderDiagnosticTestsTable(tests);
  } catch (err) {
    showToast(`Failed to load diagnostic tests: ${err.message}`, 'danger');
  }
}

function renderDiagnosticTestsTable(tests) {
  const tbody = document.getElementById('diagnostics-table-body');
  if (!tests || tests.length === 0) {
    tbody.innerHTML = `<tr><td colspan="8" style="text-align:center; color:var(--text-muted); padding:24px;">No diagnostic lab tests found</td></tr>`;
    return;
  }

  tbody.innerHTML = tests.map(t => `
    <tr>
      <td><strong>#${t.testId}</strong></td>
      <td><strong>${escapeHtml(t.patientName)}</strong></td>
      <td><strong>${escapeHtml(t.serviceName)}</strong></td>
      <td>
        <span class="badge badge-dept">${escapeHtml(t.category)}</span><br>
        <span style="font-size:12px; font-weight:700;">$${t.cost}</span>
      </td>
      <td>${escapeHtml(t.bookingDate)}</td>
      <td>${getStatusBadge(t.status)}</td>
      <td>
        <div style="max-width:240px; font-size:12px;">
          ${t.results ? `<strong>Results:</strong> ${escapeHtml(t.results)}<br>` : ''}
          ${t.normalRange ? `<span style="color:var(--text-muted);">Ref: ${escapeHtml(t.normalRange)}</span>` : '<span style="color:var(--text-light);">Awaiting results</span>'}
        </div>
      </td>
      <td>
        <button class="btn-primary" style="padding:4px 10px; font-size:12px;" onclick="openTestResultsModal(${t.testId})">
          Update Findings
        </button>
      </td>
    </tr>
  `).join('');
}

function openDiagnosticModal() {
  const form = document.getElementById('form-diagnostic');
  form.reset();
  
  document.getElementById('diag-date').value = new Date().toISOString().split('T')[0];
  populatePatientDropdown('diag-patient');
  
  openModal('modal-diagnostic');
}

async function handleOrderTest(e) {
  e.preventDefault();
  const docVal = document.getElementById('diag-doctor').value;
  const payload = {
    patientId: parseInt(document.getElementById('diag-patient').value),
    serviceId: parseInt(document.getElementById('diag-service').value),
    doctorId: docVal ? parseInt(docVal) : null,
    bookingDate: document.getElementById('diag-date').value,
    remarks: document.getElementById('diag-remarks').value.trim(),
    status: 'ORDERED'
  };

  try {
    await apiFetch('/api/diagnostics/tests', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    showToast('Diagnostic lab test requisition ordered successfully!', 'success');
    closeModal('modal-diagnostic');
    loadDiagnosticTests();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to order test: ${err.message}`, 'danger');
  }
}

async function openTestResultsModal(testId) {
  try {
    const test = await apiFetch(`/api/diagnostics/tests/${testId}`);
    document.getElementById('results-test-id').value = test.testId;
    document.getElementById('results-status').value = test.status === 'ORDERED' ? 'SAMPLE_COLLECTED' : test.status;
    document.getElementById('results-text').value = test.results || '';
    document.getElementById('results-normal-range').value = test.normalRange || '';
    document.getElementById('results-remarks').value = test.remarks || '';

    openModal('modal-test-results');
  } catch (err) {
    showToast(`Failed to fetch test: ${err.message}`, 'danger');
  }
}

async function handleSaveTestResults(e) {
  e.preventDefault();
  const testId = document.getElementById('results-test-id').value;
  const payload = {
    status: document.getElementById('results-status').value,
    results: document.getElementById('results-text').value.trim(),
    normalRange: document.getElementById('results-normal-range').value.trim(),
    remarks: document.getElementById('results-remarks').value.trim()
  };

  try {
    await apiFetch(`/api/diagnostics/tests/${testId}/results`, {
      method: 'PATCH',
      body: JSON.stringify(payload)
    });
    showToast('Diagnostic findings & status verified!', 'success');
    closeModal('modal-test-results');
    loadDiagnosticTests();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to update test results: ${err.message}`, 'danger');
  }
}

// ==============================================================================
// 7. BILLING & INVOICES MODULE
// ==============================================================================
async function loadBillingInvoices() {
  try {
    const invoices = await apiFetch('/api/billing/invoices');
    state.invoices = invoices;
    renderBillingTable(invoices);
  } catch (err) {
    showToast(`Failed to load invoices: ${err.message}`, 'danger');
  }
}

function renderBillingTable(invoices) {
  const tbody = document.getElementById('billing-table-body');
  if (!invoices || invoices.length === 0) {
    tbody.innerHTML = `<tr><td colspan="9" style="text-align:center; color:var(--text-muted); padding:24px;">No billing invoices found</td></tr>`;
    return;
  }

  tbody.innerHTML = invoices.map(i => `
    <tr>
      <td><strong>${escapeHtml(i.invoiceNumber)}</strong></td>
      <td><strong>${escapeHtml(i.patientName)}</strong></td>
      <td>${escapeHtml(i.invoiceDate)}</td>
      <td>${formatCurrency(i.subtotal)}</td>
      <td>+${formatCurrency(i.tax || 0)} / -${formatCurrency(i.discount || 0)}</td>
      <td><strong style="color:var(--text-main); font-size:14.5px;">${formatCurrency(i.totalAmount)}</strong></td>
      <td>${getStatusBadge(i.paymentStatus)}</td>
      <td><span style="color:var(--success); font-weight:700;">${formatCurrency(i.paidAmount || 0)}</span></td>
      <td>
        <div class="action-btns">
          <button class="btn-icon" title="View / Print Receipt" onclick="viewPrintableInvoice(${i.invoiceId})">
            <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="6 9 6 2 18 2 18 9"/><path d="M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect x="6" y="14" width="12" height="8"/></svg>
          </button>
          ${i.paymentStatus !== 'PAID' ? `
            <button class="btn-primary" style="padding:4px 10px; font-size:12px;" onclick="openPaymentModal(${i.invoiceId}, ${i.totalAmount - (i.paidAmount || 0)})">
              Pay
            </button>
          ` : ''}
        </div>
      </td>
    </tr>
  `).join('');
}

function openInvoiceModal() {
  const form = document.getElementById('form-invoice');
  form.reset();
  
  document.getElementById('invoice-date').value = new Date().toISOString().split('T')[0];
  populatePatientDropdown('invoice-patient');

  const container = document.getElementById('invoice-items-container');
  container.innerHTML = '';
  addInvoiceItemRow('Physician Consultation Fee', 'CONSULTATION', 1, 80.00);

  openModal('modal-invoice');
}

function addInvoiceItemRow(desc = '', type = 'PROCEDURE', qty = 1, price = 50.00) {
  const container = document.getElementById('invoice-items-container');
  const div = document.createElement('div');
  div.className = 'invoice-item-row';
  div.style.display = 'grid';
  div.style.gridTemplateColumns = '3fr 1.5fr 1fr 1.5fr 24px';
  div.style.gap = '8px';
  div.style.alignItems = 'center';

  div.innerHTML = `
    <input type="text" class="form-control item-desc" placeholder="Service / Test Description" value="${escapeHtml(desc)}" required>
    <select class="form-control item-type">
      <option value="CONSULTATION" ${type === 'CONSULTATION' ? 'selected' : ''}>Consultation</option>
      <option value="DIAGNOSTIC" ${type === 'DIAGNOSTIC' ? 'selected' : ''}>Diagnostic Lab</option>
      <option value="MEDICATION" ${type === 'MEDICATION' ? 'selected' : ''}>Medication</option>
      <option value="PROCEDURE" ${type === 'PROCEDURE' ? 'selected' : ''}>Procedure</option>
    </select>
    <input type="number" class="form-control item-qty" placeholder="Qty" value="${qty}" min="1" required>
    <input type="number" step="0.5" class="form-control item-price" placeholder="Unit Price ($)" value="${price}" required>
    <button type="button" class="btn-icon btn-danger" onclick="this.parentElement.remove()" title="Remove Item">&times;</button>
  `;
  container.appendChild(div);
}

async function handleCreateInvoice(e) {
  e.preventDefault();
  
  const itemRows = document.querySelectorAll('.invoice-item-row');
  const items = [];
  itemRows.forEach(row => {
    const desc = row.querySelector('.item-desc').value.trim();
    const type = row.querySelector('.item-type').value;
    const qty = parseInt(row.querySelector('.item-qty').value) || 1;
    const price = parseFloat(row.querySelector('.item-price').value) || 0;
    if (desc) {
      items.push({
        description: desc,
        itemType: type,
        quantity: qty,
        unitPrice: price,
        totalPrice: qty * price
      });
    }
  });

  if (items.length === 0) {
    showToast('Please add at least one line item to the invoice.', 'warning');
    return;
  }

  const taxRate = parseFloat(document.getElementById('invoice-tax-rate').value) || 0;
  const discount = parseFloat(document.getElementById('invoice-discount').value) || 0;

  const payload = {
    patientId: parseInt(document.getElementById('invoice-patient').value),
    invoiceDate: document.getElementById('invoice-date').value,
    discount: discount,
    items: items
  };

  try {
    await apiFetch('/api/billing/invoices', {
      method: 'POST',
      body: JSON.stringify(payload)
    });
    showToast('Invoice generated successfully & itemized!', 'success');
    closeModal('modal-invoice');
    loadBillingInvoices();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to generate invoice: ${err.message}`, 'danger');
  }
}

async function viewPrintableInvoice(invoiceId) {
  try {
    const inv = await apiFetch(`/api/billing/invoices/${invoiceId}`);
    const paper = document.getElementById('invoice-printable-content');

    paper.innerHTML = `
      <div class="invoice-header-block">
        <div class="hospital-brand">
          <h2>Nexus Healthcare Systems</h2>
          <p>450 Medical Arts Pavilion, Healthcare Blvd<br>Phone: +1 (800) 555-HMS1 • contact@nexushealth.org</p>
        </div>
        <div class="invoice-details-meta">
          <h4>TAX INVOICE</h4>
          <div>Invoice #: <strong>${escapeHtml(inv.invoiceNumber)}</strong></div>
          <div>Date: <strong>${escapeHtml(inv.invoiceDate)}</strong></div>
          <div style="margin-top:6px;">Status: ${getStatusBadge(inv.paymentStatus)}</div>
        </div>
      </div>

      <div class="invoice-patient-box">
        <div>
          <span style="font-size:11px; text-transform:uppercase; color:var(--text-muted); font-weight:700;">Billed To Patient:</span><br>
          <strong style="font-size:15px;">${escapeHtml(inv.patientName)}</strong><br>
          <span>${escapeHtml(inv.patientAddress || 'Residential Address on File')}</span><br>
          <span>Phone: ${escapeHtml(inv.patientPhone || 'N/A')}</span>
        </div>
        <div style="text-align:right;">
          <span style="font-size:11px; text-transform:uppercase; color:var(--text-muted); font-weight:700;">Payment Details:</span><br>
          <span>Method: <strong>${escapeHtml(inv.paymentMethod || 'Pending Payment')}</strong></span><br>
          <span>Amount Paid: <strong>${formatCurrency(inv.paidAmount || 0)}</strong></span>
        </div>
      </div>

      <table class="custom-table invoice-summary-table">
        <thead>
          <tr>
            <th>#</th>
            <th>Item / Service Description</th>
            <th>Type</th>
            <th style="text-align:center;">Qty</th>
            <th style="text-align:right;">Unit Price</th>
            <th style="text-align:right;">Total</th>
          </tr>
        </thead>
        <tbody>
          ${(inv.items || []).map((item, idx) => `
            <tr>
              <td>${idx + 1}</td>
              <td><strong>${escapeHtml(item.description)}</strong></td>
              <td><span class="badge badge-dept">${escapeHtml(item.itemType)}</span></td>
              <td style="text-align:center;">${item.quantity}</td>
              <td style="text-align:right;">${formatCurrency(item.unitPrice)}</td>
              <td style="text-align:right;"><strong>${formatCurrency(item.totalPrice)}</strong></td>
            </tr>
          `).join('')}
        </tbody>
      </table>

      <div class="invoice-totals-box">
        <div class="invoice-total-row">
          <span>Subtotal:</span>
          <span>${formatCurrency(inv.subtotal)}</span>
        </div>
        <div class="invoice-total-row">
          <span>Healthcare Tax (5%):</span>
          <span>+${formatCurrency(inv.tax || 0)}</span>
        </div>
        <div class="invoice-total-row">
          <span>Discount Applied:</span>
          <span>-${formatCurrency(inv.discount || 0)}</span>
        </div>
        <div class="invoice-total-row grand-total">
          <span>Total Payable:</span>
          <span>${formatCurrency(inv.totalAmount)}</span>
        </div>
      </div>

      <div style="margin-top:32px; border-top:1px dashed var(--border); padding-top:16px; display:flex; justify-content:space-between; font-size:11px; color:var(--text-muted);">
        <div>Thank you for choosing Nexus Healthcare. This is a computer generated invoice.</div>
        <div style="text-align:right;">Authorized Financial Officer Signature: __________________</div>
      </div>
    `;

    openModal('modal-invoice-view');
  } catch (err) {
    showToast(`Failed to open invoice: ${err.message}`, 'danger');
  }
}

function openPaymentModal(invoiceId, dueAmount) {
  document.getElementById('payment-invoice-id').value = invoiceId;
  document.getElementById('payment-amount-due').value = dueAmount.toFixed(2);
  document.getElementById('payment-amount-paid').value = dueAmount.toFixed(2);
  openModal('modal-payment');
}

async function handleRecordPayment(e) {
  e.preventDefault();
  const invoiceId = document.getElementById('payment-invoice-id').value;
  const paidAmount = parseFloat(document.getElementById('payment-amount-paid').value);
  const paymentMethod = document.getElementById('payment-method').value;

  try {
    await apiFetch(`/api/billing/invoices/${invoiceId}/payment`, {
      method: 'POST',
      body: JSON.stringify({
        paidAmount: paidAmount,
        paymentMethod: paymentMethod
      })
    });
    showToast(`Payment of $${paidAmount.toFixed(2)} recorded & receipt updated!`, 'success');
    closeModal('modal-payment');
    loadBillingInvoices();
    loadDashboard();
  } catch (err) {
    showToast(`Failed to record payment: ${err.message}`, 'danger');
  }
}

// ==============================================================================
// 8. AUDIT TRAIL / EDIT HISTORY MODULE
// ==============================================================================
async function loadAuditLogs() {
  try {
    const logs = await apiFetch('/api/audit-logs?limit=50');
    state.auditLogs = logs;
    renderAuditTable(logs);
  } catch (err) {
    showToast(`Failed to load audit trail: ${err.message}`, 'danger');
  }
}

function renderAuditTable(logs) {
  const tbody = document.getElementById('audit-table-body');
  if (!logs || logs.length === 0) {
    tbody.innerHTML = `<tr><td colspan="10" style="text-align:center; color:var(--text-muted); padding:24px;">No audit trail events found</td></tr>`;
    return;
  }

  tbody.innerHTML = logs.map(l => `
    <tr>
      <td><strong>#${l.logId}</strong></td>
      <td><span class="badge badge-dept">${escapeHtml(l.entityType)}</span></td>
      <td><strong>#${l.entityId}</strong></td>
      <td>${getActionBadge(l.actionType)}</td>
      <td><code>${escapeHtml(l.fieldName || 'ALL')}</code></td>
      <td><span style="color:#b91c1c; font-size:12px;">${escapeHtml(l.oldValue || '-')}</span></td>
      <td><span style="color:#15803d; font-size:12px; font-weight:600;">${escapeHtml(l.newValue || '-')}</span></td>
      <td><strong>${escapeHtml(l.modifiedBy || 'System')}</strong></td>
      <td><span style="font-size:11px; font-family:var(--font-mono);">${formatDate(l.timestamp)}</span></td>
      <td><span style="font-size:12px; color:var(--text-muted);">${escapeHtml(l.reasonOrNotes || '')}</span></td>
    </tr>
  `).join('');
}

// ==============================================================================
// MODAL & UI HELPERS
// ==============================================================================
function openModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.add('active');
}

function closeModal(id) {
  const modal = document.getElementById(id);
  if (modal) modal.classList.remove('active');
}

// Close modals when clicking outside dialog
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      overlay.classList.remove('active');
    }
  });
});

function populatePatientDropdown(selectId) {
  const select = document.getElementById(selectId);
  if (!select) return;
  if (!state.patients || state.patients.length === 0) {
    select.innerHTML = '<option value="">No patients available</option>';
    return;
  }
  select.innerHTML = state.patients.map(p => `
    <option value="${p.patientId}">#${p.patientId} - ${escapeHtml(p.fullName)} (${escapeHtml(p.phone)})</option>
  `).join('');
}

// Status Badges
function getStatusBadge(status) {
  if (!status) return '';
  const s = status.toUpperCase();
  switch(s) {
    case 'SCHEDULED': return `<span class="badge badge-scheduled">&bull; Scheduled</span>`;
    case 'CONFIRMED': return `<span class="badge badge-confirmed">&bull; Confirmed</span>`;
    case 'COMPLETED': return `<span class="badge badge-completed">&bull; Completed</span>`;
    case 'CANCELLED': return `<span class="badge badge-cancelled">&bull; Cancelled</span>`;
    case 'PAID': return `<span class="badge badge-paid">&bull; Paid</span>`;
    case 'UNPAID': return `<span class="badge badge-unpaid">&bull; Unpaid</span>`;
    case 'PARTIALLY_PAID': return `<span class="badge badge-partial">&bull; Partial</span>`;
    case 'ORDERED': return `<span class="badge badge-ordered">&bull; Ordered</span>`;
    case 'SAMPLE_COLLECTED': return `<span class="badge badge-sample">&bull; Sample Collected</span>`;
    case 'IN_PROGRESS': return `<span class="badge badge-in-progress">&bull; In Progress</span>`;
    default: return `<span class="badge">${escapeHtml(status)}</span>`;
  }
}

function getActionBadge(action) {
  const a = (action || '').toUpperCase();
  switch(a) {
    case 'CREATE': return `<span class="badge" style="background:#ecfdf5; color:#047857; font-weight:700;">CREATE</span>`;
    case 'UPDATE': return `<span class="badge" style="background:#eff6ff; color:#1d4ed8; font-weight:700;">UPDATE</span>`;
    case 'STATUS_CHANGE': return `<span class="badge" style="background:#fef3c7; color:#b45309; font-weight:700;">STATUS</span>`;
    case 'DELETE': return `<span class="badge" style="background:#fee2e2; color:#b91c1c; font-weight:700;">DELETE</span>`;
    default: return `<span class="badge">${escapeHtml(action)}</span>`;
  }
}

// Formatting Utilities
function formatCurrency(amt) {
  const val = parseFloat(amt) || 0;
  return '$' + val.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDate(isoStr) {
  if (!isoStr) return '';
  try {
    const d = new Date(isoStr);
    return d.toLocaleString([], { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  } catch (e) {
    return isoStr;
  }
}

function getInitials(name) {
  if (!name) return 'PT';
  const parts = name.replace(/Dr\.\s*/i, '').trim().split(' ');
  if (parts.length >= 2) {
    return (parts[0][0] + parts[1][0]).toUpperCase();
  }
  return (name[0] || 'P').toUpperCase();
}

function escapeHtml(str) {
  if (str === null || str === undefined) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

// Toast Notifications
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ==============================================================================
// BACKEND CONNECTION CONFIGURATION (Vercel <-> Render)
// ==============================================================================
function initBackendConfigUI() {
  const statusContainer = document.querySelector('.sidebar-footer');
  const statusText = document.querySelector('.system-status span:last-child');
  const currentUrl = getApiUrl('');

  if (statusText) {
    if (currentUrl) {
      statusText.textContent = `Cloud: ${currentUrl.replace(/^https?:\/\//, '').split('/')[0]}`;
      statusText.title = `Connected to: ${currentUrl} (Click to configure)`;
    } else {
      statusText.textContent = 'JDBC & Database (Local/Proxy)';
      statusText.title = 'Running on local server. Click to configure cloud backend URL.';
    }
  }

  if (statusContainer) {
    statusContainer.style.cursor = 'pointer';
    statusContainer.title = 'Click to configure Backend URL (Render)';
    statusContainer.addEventListener('click', () => {
      openBackendSettingsModal();
    });
  }

  // Bind settings form
  const form = document.getElementById('form-backend-settings');
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const inputVal = document.getElementById('backend-api-url-input').value.trim();
      if (inputVal) {
        localStorage.setItem('HMS_BACKEND_URL', inputVal);
        showToast('Backend URL saved! Reloading data...', 'success');
      } else {
        localStorage.removeItem('HMS_BACKEND_URL');
        showToast('Reset to default same-origin / local API.', 'info');
      }
      closeModal('modal-backend-settings');
      setTimeout(() => {
        window.location.reload();
      }, 600);
    });
  }

  const testBtn = document.getElementById('btn-test-backend');
  if (testBtn) {
    testBtn.addEventListener('click', async () => {
      const inputVal = document.getElementById('backend-api-url-input').value.trim();
      const testUrl = inputVal ? inputVal.replace(/\/+$/, '') + '/api/analytics/dashboard' : '/api/analytics/dashboard';
      const resultEl = document.getElementById('backend-test-status');
      if (resultEl) {
        resultEl.innerHTML = '<span style="color:#64748b;">Testing connection...</span>';
      }
      try {
        const resp = await fetch(testUrl);
        if (resp.ok) {
          if (resultEl) resultEl.innerHTML = '<span style="color:#10b981; font-weight:600;">✓ Connected successfully to Render backend!</span>';
        } else {
          if (resultEl) resultEl.innerHTML = `<span style="color:#ef4444;">⚠ Connected but received status ${resp.status}</span>`;
        }
      } catch (err) {
        if (resultEl) resultEl.innerHTML = `<span style="color:#ef4444;">✗ Connection failed: ${err.message}</span>`;
      }
    });
  }
}

function openBackendSettingsModal() {
  const input = document.getElementById('backend-api-url-input');
  if (input) {
    input.value = localStorage.getItem('HMS_BACKEND_URL') || (window.BACKEND_API_URL || '');
  }
  const resultEl = document.getElementById('backend-test-status');
  if (resultEl) resultEl.innerHTML = '';
  openModal('modal-backend-settings');
}
