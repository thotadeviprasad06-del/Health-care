package com.healthcare.hms.controller;

import com.healthcare.hms.model.Invoice;
import com.healthcare.hms.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing")
@CrossOrigin(origins = "*")
public class BillingController {
    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/invoices")
    public List<Invoice> getAllInvoices(@RequestParam(required = false) Integer patientId) {
        if (patientId != null) {
            return billingService.getInvoicesByPatientId(patientId);
        }
        return billingService.getAllInvoices();
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Integer id) {
        return billingService.getInvoiceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> createInvoice(@RequestBody Invoice invoice,
                                                 @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        Invoice created = billingService.createInvoice(invoice, userRole != null ? userRole : "Billing Clerk");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/invoices/{id}/payment")
    public ResponseEntity<?> recordPayment(@PathVariable Integer id,
                                           @RequestBody Map<String, Object> payload,
                                           @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        Object amtObj = payload.get("paidAmount");
        BigDecimal paidAmount = BigDecimal.ZERO;
        if (amtObj != null) {
            paidAmount = new BigDecimal(amtObj.toString());
        }
        String paymentMethod = (String) payload.getOrDefault("paymentMethod", "Cash");

        boolean updated = billingService.recordPayment(id, paidAmount, paymentMethod, userRole != null ? userRole : "Billing Cashier");
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Payment processed successfully"));
        }
        return ResponseEntity.notFound().build();
    }
}
