package com.erp.model.manufacturing;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "work_orders")
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String woNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "bom_id", nullable = false)
    private BillOfMaterial billOfMaterial;

    @Column(nullable = false)
    private Integer targetQuantity;

    private Integer completedQuantity = 0;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate completionDate;
    private String supervisor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status = WorkOrderStatus.PLANNED;

    private String notes;

    public enum WorkOrderStatus {
        PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public WorkOrder() {}

    public WorkOrder(String woNumber, BillOfMaterial billOfMaterial, Integer targetQuantity,
                     LocalDate startDate, LocalDate dueDate, String supervisor, String notes) {
        this.woNumber = woNumber;
        this.billOfMaterial = billOfMaterial;
        this.targetQuantity = targetQuantity;
        this.completedQuantity = 0;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.supervisor = supervisor;
        this.notes = notes;
        this.status = WorkOrderStatus.PLANNED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWoNumber() { return woNumber; }
    public void setWoNumber(String woNumber) { this.woNumber = woNumber; }

    public BillOfMaterial getBillOfMaterial() { return billOfMaterial; }
    public void setBillOfMaterial(BillOfMaterial billOfMaterial) { this.billOfMaterial = billOfMaterial; }

    public Integer getTargetQuantity() { return targetQuantity; }
    public void setTargetQuantity(Integer targetQuantity) { this.targetQuantity = targetQuantity; }

    public Integer getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(Integer completedQuantity) { this.completedQuantity = completedQuantity; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getCompletionDate() { return completionDate; }
    public void setCompletionDate(LocalDate completionDate) { this.completionDate = completionDate; }

    public String getSupervisor() { return supervisor; }
    public void setSupervisor(String supervisor) { this.supervisor = supervisor; }

    public WorkOrderStatus getStatus() { return status; }
    public void setStatus(WorkOrderStatus status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
