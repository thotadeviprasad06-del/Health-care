package com.healthcare.hms.model;

import java.math.BigDecimal;

public class InvoiceItem {
    private Integer itemId;
    private Integer invoiceId;
    private String description;
    private String itemType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    public InvoiceItem() {}

    public InvoiceItem(Integer itemId, Integer invoiceId, String description,
                       String itemType, Integer quantity, BigDecimal unitPrice, BigDecimal totalPrice) {
        this.itemId = itemId;
        this.invoiceId = invoiceId;
        this.description = description;
        this.itemType = itemType;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
    }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public Integer getInvoiceId() { return invoiceId; }
    public void setInvoiceId(Integer invoiceId) { this.invoiceId = invoiceId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
