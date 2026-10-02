package com.erp.model.finance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceType type; // AR_SALES, AP_PURCHASE

    private String partyName; // Customer Name or Vendor Name
    private String referenceOrder; // SO-001 or PO-001
    private LocalDate invoiceDate;
    private LocalDate dueDate;
    private BigDecimal amount = BigDecimal.ZERO;
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status = InvoiceStatus.UNPAID;

    public enum InvoiceType {
        AR_SALES, AP_PURCHASE
    }

    public enum InvoiceStatus {
        UNPAID, PARTIALLY_PAID, PAID, CANCELLED
    }

    public Invoice() {}

    public Invoice(String invoiceNumber, InvoiceType type, String partyName, String referenceOrder,
                   LocalDate invoiceDate, LocalDate dueDate, BigDecimal amount) {
        this.invoiceNumber = invoiceNumber;
        this.type = type;
        this.partyName = partyName;
        this.referenceOrder = referenceOrder;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.status = InvoiceStatus.UNPAID;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public InvoiceType getType() { return type; }
    public void setType(InvoiceType type) { this.type = type; }

    public String getPartyName() { return partyName; }
    public void setPartyName(String partyName) { this.partyName = partyName; }

    public String getReferenceOrder() { return referenceOrder; }
    public void setReferenceOrder(String referenceOrder) { this.referenceOrder = referenceOrder; }

    public LocalDate getInvoiceDate() { return invoiceDate; }
    public void setInvoiceDate(LocalDate invoiceDate) { this.invoiceDate = invoiceDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
}
