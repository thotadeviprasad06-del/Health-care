package com.healthcare.hms.dao;

import com.healthcare.hms.model.Invoice;
import com.healthcare.hms.model.InvoiceItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BillingDAO {
    private final JdbcTemplate jdbcTemplate;

    public BillingDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Invoice> invoiceRowMapper = (rs, rowNum) -> {
        Invoice inv = new Invoice();
        inv.setInvoiceId(rs.getInt("invoice_id"));
        inv.setPatientId(rs.getInt("patient_id"));
        inv.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null);
        inv.setInvoiceNumber(rs.getString("invoice_number"));
        Date d = rs.getDate("invoice_date");
        if (d != null) inv.setInvoiceDate(d.toLocalDate());
        inv.setSubtotal(rs.getBigDecimal("subtotal"));
        inv.setTax(rs.getBigDecimal("tax"));
        inv.setDiscount(rs.getBigDecimal("discount"));
        inv.setTotalAmount(rs.getBigDecimal("total_amount"));
        inv.setPaymentStatus(rs.getString("payment_status"));
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setPaidAmount(rs.getBigDecimal("paid_amount"));
        inv.setNotes(rs.getString("notes"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) inv.setCreatedAt(ca.toLocalDateTime());

        try {
            inv.setPatientName(rs.getString("first_name") + " " + rs.getString("last_name"));
            inv.setPatientPhone(rs.getString("phone"));
            inv.setPatientEmail(rs.getString("email"));
            inv.setPatientAddress(rs.getString("address"));
        } catch (Exception ignored) {}

        return inv;
    };

    private final RowMapper<InvoiceItem> itemRowMapper = (rs, rowNum) -> {
        InvoiceItem item = new InvoiceItem();
        item.setItemId(rs.getInt("item_id"));
        item.setInvoiceId(rs.getInt("invoice_id"));
        item.setDescription(rs.getString("description"));
        item.setItemType(rs.getString("item_type"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setTotalPrice(rs.getBigDecimal("total_price"));
        return item;
    };

    public List<Invoice> findAll() {
        String sql = "SELECT i.*, p.first_name, p.last_name, p.phone, p.email, p.address " +
                     "FROM invoices i " +
                     "JOIN patients p ON i.patient_id = p.patient_id " +
                     "ORDER BY i.invoice_date DESC, i.invoice_id DESC";
        List<Invoice> list = jdbcTemplate.query(sql, invoiceRowMapper);
        for (Invoice inv : list) {
            inv.setItems(findItemsByInvoiceId(inv.getInvoiceId()));
        }
        return list;
    }

    public Optional<Invoice> findById(Integer invoiceId) {
        String sql = "SELECT i.*, p.first_name, p.last_name, p.phone, p.email, p.address " +
                     "FROM invoices i " +
                     "JOIN patients p ON i.patient_id = p.patient_id " +
                     "WHERE i.invoice_id = ?";
        List<Invoice> list = jdbcTemplate.query(sql, invoiceRowMapper, invoiceId);
        if (list.isEmpty()) return Optional.empty();

        Invoice inv = list.get(0);
        inv.setItems(findItemsByInvoiceId(invoiceId));
        return Optional.of(inv);
    }

    public List<Invoice> findByPatientId(Integer patientId) {
        String sql = "SELECT i.*, p.first_name, p.last_name, p.phone, p.email, p.address " +
                     "FROM invoices i " +
                     "JOIN patients p ON i.patient_id = p.patient_id " +
                     "WHERE i.patient_id = ? " +
                     "ORDER BY i.invoice_date DESC";
        List<Invoice> list = jdbcTemplate.query(sql, invoiceRowMapper, patientId);
        for (Invoice inv : list) {
            inv.setItems(findItemsByInvoiceId(inv.getInvoiceId()));
        }
        return list;
    }

    public Invoice save(Invoice invoice) {
        String sql = "INSERT INTO invoices (patient_id, appointment_id, invoice_number, invoice_date, subtotal, tax, discount, total_amount, payment_status, payment_method, paid_amount, notes, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, invoice.getPatientId());
            if (invoice.getAppointmentId() != null) ps.setInt(2, invoice.getAppointmentId()); else ps.setNull(2, java.sql.Types.INTEGER);
            ps.setString(3, invoice.getInvoiceNumber());
            ps.setDate(4, Date.valueOf(invoice.getInvoiceDate()));
            ps.setBigDecimal(5, invoice.getSubtotal());
            ps.setBigDecimal(6, invoice.getTax() != null ? invoice.getTax() : BigDecimal.ZERO);
            ps.setBigDecimal(7, invoice.getDiscount() != null ? invoice.getDiscount() : BigDecimal.ZERO);
            ps.setBigDecimal(8, invoice.getTotalAmount());
            ps.setString(9, invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "UNPAID");
            ps.setString(10, invoice.getPaymentMethod());
            ps.setBigDecimal(11, invoice.getPaidAmount() != null ? invoice.getPaidAmount() : BigDecimal.ZERO);
            ps.setString(12, invoice.getNotes());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    invoice.setInvoiceId(n.intValue());
                    break;
                }
            }
        }

        // Save Items
        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                item.setInvoiceId(invoice.getInvoiceId());
                saveItem(item);
            }
        }

        return invoice;
    }

    public List<InvoiceItem> findItemsByInvoiceId(Integer invoiceId) {
        String sql = "SELECT * FROM invoice_items WHERE invoice_id = ? ORDER BY item_id ASC";
        return jdbcTemplate.query(sql, itemRowMapper, invoiceId);
    }

    public InvoiceItem saveItem(InvoiceItem item) {
        String sql = "INSERT INTO invoice_items (invoice_id, description, item_type, quantity, unit_price, total_price) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, item.getInvoiceId());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getItemType());
            ps.setInt(4, item.getQuantity() != null ? item.getQuantity() : 1);
            ps.setBigDecimal(5, item.getUnitPrice());
            ps.setBigDecimal(6, item.getTotalPrice());
            return ps;
        }, keyHolder);

        if (!keyHolder.getKeyList().isEmpty()) {
            Map<String, Object> m = keyHolder.getKeyList().get(0);
            for (Object v : m.values()) {
                if (v instanceof Number n) {
                    item.setItemId(n.intValue());
                    break;
                }
            }
        }
        return item;
    }

    public boolean updatePaymentStatus(Integer invoiceId, String status, String method, BigDecimal paidAmount) {
        String sql = "UPDATE invoices SET payment_status = ?, payment_method = COALESCE(?, payment_method), paid_amount = ? WHERE invoice_id = ?";
        return jdbcTemplate.update(sql, status, method, paidAmount, invoiceId) > 0;
    }
}
