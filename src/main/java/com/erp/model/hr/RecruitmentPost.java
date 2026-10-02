package com.erp.model.hr;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recruitment_posts")
public class RecruitmentPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String postingCode; // e.g. POST-2026-001

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "requisition_id")
    private JobRequisition jobRequisition;

    @Column(nullable = false)
    private String jobTitle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    private String team;
    private String location = "Dhaka, Bangladesh / Silicon Valley (Hybrid)";
    private String employmentType = "Full-time";
    private String experienceLevel = "Senior";
    private String salaryDisplay; // e.g. "$110,000 - $135,000 / year" or "BDT 150,000 - 200,000"

    private String summary; // Headline for LinkedIn/social posts

    @Column(length = 4000)
    private String description;

    @Column(length = 2000)
    private String requirements;

    @Column(length = 1000)
    private String benefits;

    private LocalDate applicationDeadline;
    private LocalDate postedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status = PostStatus.PUBLISHED;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<RecruitmentPostDistribution> distributions = new ArrayList<>();

    public enum PostStatus {
        DRAFT,
        PUBLISHED,
        CLOSED,
        ARCHIVED
    }

    public RecruitmentPost() {}

    public RecruitmentPost(String postingCode, JobRequisition jobRequisition, String jobTitle,
                           Department department, String team, String location, String employmentType,
                           String experienceLevel, String salaryDisplay, String summary,
                           String description, String requirements, String benefits,
                           LocalDate applicationDeadline) {
        this.postingCode = postingCode;
        this.jobRequisition = jobRequisition;
        this.jobTitle = jobTitle;
        this.department = department;
        this.team = team;
        this.location = location != null ? location : "Dhaka, Bangladesh / Silicon Valley (Hybrid)";
        this.employmentType = employmentType != null ? employmentType : "Full-time";
        this.experienceLevel = experienceLevel != null ? experienceLevel : "Senior";
        this.salaryDisplay = salaryDisplay;
        this.summary = summary;
        this.description = description;
        this.requirements = requirements;
        this.benefits = benefits;
        this.applicationDeadline = applicationDeadline;
        this.postedDate = LocalDate.now();
        this.status = PostStatus.PUBLISHED;
    }

    public void addDistribution(RecruitmentPostDistribution dist) {
        dist.setPost(this);
        this.distributions.add(dist);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPostingCode() { return postingCode; }
    public void setPostingCode(String postingCode) { this.postingCode = postingCode; }

    public JobRequisition getJobRequisition() { return jobRequisition; }
    public void setJobRequisition(JobRequisition jobRequisition) { this.jobRequisition = jobRequisition; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getSalaryDisplay() { return salaryDisplay; }
    public void setSalaryDisplay(String salaryDisplay) { this.salaryDisplay = salaryDisplay; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getBenefits() { return benefits; }
    public void setBenefits(String benefits) { this.benefits = benefits; }

    public LocalDate getApplicationDeadline() { return applicationDeadline; }
    public void setApplicationDeadline(LocalDate applicationDeadline) { this.applicationDeadline = applicationDeadline; }

    public LocalDate getPostedDate() { return postedDate; }
    public void setPostedDate(LocalDate postedDate) { this.postedDate = postedDate; }

    public PostStatus getStatus() { return status; }
    public void setStatus(PostStatus status) { this.status = status; }

    public List<RecruitmentPostDistribution> getDistributions() { return distributions; }
    public void setDistributions(List<RecruitmentPostDistribution> distributions) { this.distributions = distributions; }
}
