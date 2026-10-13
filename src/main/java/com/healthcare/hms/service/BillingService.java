package com.healthcare.hms.service;

import com.healthcare.hms.dao.AuditLogDAO;
import com.healthcare.hms.dao.BillingDAO;
import com.healthcare.hms.model.AuditLog;
import com.healthcare.hms.model.Invoice;
import com.healthcare.hms.model.InvoiceItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillingService {
    private final BillingDAO billingDAO;
    private final AuditLogDAO auditLogDAO;

    public BillingService(BillingDAO billingDAO, AuditLogDAO auditLogDAO) {
        this.billingDAO = billingDAO;
        this.auditLogDAO = auditLogDAO;
    }

    public List<Invoice> getAllInvoices() {
        return billingDAO.findAll();
    }

    public Optional<Invoice> getInvoiceById(Integer invoiceId) {
        return billingDAO.findById(invoiceId);
    }

    public List<Invoice> getInvoicesByPatientId(Integer patientId) {
        return billingDAO.findByPatientId(patientId);
    }

    @Transactional
    public Invoice createInvoice(Invoice invoice, String user) {
        if (invoice.getInvoiceDate() == null) {
            invoice.setInvoiceDate(LocalDate.now());
        }
        if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().trim().isEmpty()) {
            invoice.setInvoiceNumber("INV-" + LocalDate.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        }

        // Recalculate totals from items
        BigDecimal subtotal = BigDecimal.ZERO;
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                if (item.getQuantity() == null || item.getQuantity() <= 0) item.setQuantity(1);
                if (item.getUnitPrice() == null) item.setUnitPrice(BigDecimal.ZERO);
                BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                item.setTotalPrice(lineTotal);
                subtotal = subtotal.add(lineTotal);
            }
        }
        invoice.setSubtotal(subtotal);

        // Tax calculation (e.g. 5% tax if not specified)
        if (invoice.getTax() == null) {
            invoice.setTax(subtotal.multiply(new BigDecimal("0.05")));
        }
        if (invoice.getDiscount() == null) {
            invoice.setDiscount(BigDecimal.ZERO);
        }

        BigDecimal total = subtotal.add(invoice.getTax()).subtract(invoice.getDiscount());
        invoice.setTotalAmount(total.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : total);

        if (invoice.getPaymentStatus() == null) {
            invoice.setPaymentStatus("UNPAID");
        }

        Invoice saved = billingDAO.save(invoice);

        // Audit Trail
        AuditLog log = new AuditLog();
        log.setEntityType("INVOICE");
        log.setEntityId(saved.getInvoiceId());
        log.setActionType("CREATE");
        log.setFieldName("TOTAL_AMOUNT");
        log.setOldValue(null);
        log.setNewValue("$" + saved.getTotalAmount() + " (" + saved.getInvoiceNumber() + ")");
        log.setModifiedBy(user != null ? user : "Billing Clerk");
        log.setReasonOrNotes("Itemized healthcare invoice generated.");
        auditLogDAO.save(log);

        return saved;
    }

    @Transactional
    public boolean recordPayment(Integer invoiceId, BigDecimal paidAmount, String paymentMethod, String user) {
        Optional<Invoice> existingOpt = billingDAO.findById(invoiceId);
        if (existingOpt.isEmpty()) return false;

        Invoice existing = existingOpt.get();
        BigDecimal currentPaid = existing.getPaidAmount() != null ? existing.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newTotalPaid = currentPaid.add(paidAmount);

        String newStatus;
        if (newTotalPaid.compareTo(existing.getTotalAmount()) >= 0) {
            newStatus = "PAID";
        } else if (newTotalPaid.compareTo(BigDecimal.ZERO) > 0) {
            newStatus = "PARTIALLY_PAID";
        } else {
            newStatus = "UNPAID";
        }

        boolean updated = billingDAO.updatePaymentStatus(invoiceId, newStatus, paymentMethod, newTotalPaid);
        if (updated) {
            AuditLog log = new AuditLog();
            log.setEntityType("INVOICE");
            log.setEntityId(invoiceId);
            log.setActionType("STATUS_CHANGE");
            log.setFieldName("payment_status");
            log.setOldValue(existing.getPaymentStatus());
            log.setNewValue(newStatus);
            log.setModifiedBy(user != null ? user : "Billing Cashier");
            log.setReasonOrNotes("Payment of $" + paidAmount + " received via " + paymentMethod + ". Total paid: $" + newTotalPaid);
            auditLogDAO.save(log);
        }
        return updated;
    }
}
