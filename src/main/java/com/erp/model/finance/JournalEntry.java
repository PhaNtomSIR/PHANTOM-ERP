package com.erp.model.finance;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String entryNumber; // e.g. JE-2026-001

    private LocalDate entryDate;
    private String description;
    private String referenceType; // SALES, PROCUREMENT, PAYROLL, MANUFACTURING, MANUAL
    private String referenceNumber; // SO-001, PO-001, etc.

    private BigDecimal totalDebit = BigDecimal.ZERO;
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EntryStatus status = EntryStatus.POSTED;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<JournalLine> lines = new ArrayList<>();

    public enum EntryStatus {
        DRAFT, POSTED, REVERSED
    }

    public JournalEntry() {}

    public JournalEntry(String entryNumber, LocalDate entryDate, String description,
                        String referenceType, String referenceNumber) {
        this.entryNumber = entryNumber;
        this.entryDate = entryDate;
        this.description = description;
        this.referenceType = referenceType;
        this.referenceNumber = referenceNumber;
        this.status = EntryStatus.POSTED;
    }

    public void addLine(JournalLine line) {
        lines.add(line);
        line.setJournalEntry(this);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.totalDebit = lines.stream()
                .map(JournalLine::getDebit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalCredit = lines.stream()
                .map(JournalLine::getCredit)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isBalanced() {
        return totalDebit != null && totalCredit != null && totalDebit.compareTo(totalCredit) == 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEntryNumber() { return entryNumber; }
    public void setEntryNumber(String entryNumber) { this.entryNumber = entryNumber; }

    public LocalDate getEntryDate() { return entryDate; }
    public void setEntryDate(LocalDate entryDate) { this.entryDate = entryDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public BigDecimal getTotalDebit() { return totalDebit; }
    public void setTotalDebit(BigDecimal totalDebit) { this.totalDebit = totalDebit; }

    public BigDecimal getTotalCredit() { return totalCredit; }
    public void setTotalCredit(BigDecimal totalCredit) { this.totalCredit = totalCredit; }

    public EntryStatus getStatus() { return status; }
    public void setStatus(EntryStatus status) { this.status = status; }

    public List<JournalLine> getLines() { return lines; }
    public void setLines(List<JournalLine> lines) { this.lines = lines; }
}
