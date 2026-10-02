package com.erp.model.hr;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payrolls")
public class Payroll {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String payrollNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private String payPeriod; // e.g. "2026-10"
    private BigDecimal grossSalary;
    private BigDecimal deductions;
    private BigDecimal taxAmount;
    private BigDecimal netSalary;
    private LocalDate paymentDate;

    @Enumerated(EnumType.STRING)
    private PayrollStatus status = PayrollStatus.PENDING;

    public enum PayrollStatus {
        PENDING, PROCESSED, PAID
    }

    public Payroll() {}

    public Payroll(String payrollNumber, Employee employee, String payPeriod, BigDecimal grossSalary,
                   BigDecimal deductions, BigDecimal taxAmount, BigDecimal netSalary, LocalDate paymentDate) {
        this.payrollNumber = payrollNumber;
        this.employee = employee;
        this.payPeriod = payPeriod;
        this.grossSalary = grossSalary;
        this.deductions = deductions;
        this.taxAmount = taxAmount;
        this.netSalary = netSalary;
        this.paymentDate = paymentDate;
        this.status = PayrollStatus.PROCESSED;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPayrollNumber() { return payrollNumber; }
    public void setPayrollNumber(String payrollNumber) { this.payrollNumber = payrollNumber; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getPayPeriod() { return payPeriod; }
    public void setPayPeriod(String payPeriod) { this.payPeriod = payPeriod; }

    public BigDecimal getGrossSalary() { return grossSalary; }
    public void setGrossSalary(BigDecimal grossSalary) { this.grossSalary = grossSalary; }

    public BigDecimal getDeductions() { return deductions; }
    public void setDeductions(BigDecimal deductions) { this.deductions = deductions; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getNetSalary() { return netSalary; }
    public void setNetSalary(BigDecimal netSalary) { this.netSalary = netSalary; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public PayrollStatus getStatus() { return status; }
    public void setStatus(PayrollStatus status) { this.status = status; }
}
