package com.erp.model.hr;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "job_requisitions")
public class JobRequisition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String requisitionNumber; // e.g. REQ-2026-001

    @Column(nullable = false)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private String team;
    private String hiringManager;
    private Integer openingsCount = 1;

    private String jobType = "Full-time"; // Full-time, Part-time, Contract, Internship
    private String experienceLevel = "Mid-Senior"; // Entry, Mid, Senior, Lead, Executive

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequisitionStatus status = RequisitionStatus.PENDING_APPROVAL;

    private String reasonForHire; // e.g. "Department expansion for Q4", "Replacement for departed engineer"

    @Column(length = 2000)
    private String description;

    private LocalDate requestedDate;
    private LocalDate targetStartDate;

    public enum RequisitionStatus {
        DRAFT,
        PENDING_APPROVAL,
        APPROVED,
        OPEN,
        FILLED,
        CANCELLED
    }

    public JobRequisition() {}

    public JobRequisition(String requisitionNumber, String jobTitle, Department department, String team,
                          String hiringManager, Integer openingsCount, String jobType, String experienceLevel,
                          BigDecimal salaryMin, BigDecimal salaryMax, String reasonForHire,
                          String description, LocalDate targetStartDate) {
        this.requisitionNumber = requisitionNumber;
        this.jobTitle = jobTitle;
        this.department = department;
        this.team = team;
        this.hiringManager = hiringManager;
        this.openingsCount = openingsCount != null ? openingsCount : 1;
        this.jobType = jobType != null ? jobType : "Full-time";
        this.experienceLevel = experienceLevel != null ? experienceLevel : "Mid-Senior";
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.reasonForHire = reasonForHire;
        this.description = description;
        this.requestedDate = LocalDate.now();
        this.targetStartDate = targetStartDate;
        this.status = RequisitionStatus.PENDING_APPROVAL;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRequisitionNumber() { return requisitionNumber; }
    public void setRequisitionNumber(String requisitionNumber) { this.requisitionNumber = requisitionNumber; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getHiringManager() { return hiringManager; }
    public void setHiringManager(String hiringManager) { this.hiringManager = hiringManager; }

    public Integer getOpeningsCount() { return openingsCount; }
    public void setOpeningsCount(Integer openingsCount) { this.openingsCount = openingsCount; }

    public String getJobType() { return jobType; }
    public void setJobType(String jobType) { this.jobType = jobType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }

    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }

    public RequisitionStatus getStatus() { return status; }
    public void setStatus(RequisitionStatus status) { this.status = status; }

    public String getReasonForHire() { return reasonForHire; }
    public void setReasonForHire(String reasonForHire) { this.reasonForHire = reasonForHire; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDate requestedDate) { this.requestedDate = requestedDate; }

    public LocalDate getTargetStartDate() { return targetStartDate; }
    public void setTargetStartDate(LocalDate targetStartDate) { this.targetStartDate = targetStartDate; }
}
