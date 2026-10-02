package com.erp.model.hr;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_post_distributions")
public class RecruitmentPostDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    @JsonBackReference
    private RecruitmentPost post;

    @Column(nullable = false)
    private String channelCode; // LINKEDIN, BDJOBS, INDEED, CAREERS_PORTAL

    @Column(nullable = false)
    private String channelName; // e.g. LinkedIn Jobs, BDJobs Circular

    private String externalListingId; // e.g. LI-JOB-89412, BDJ-99210
    private String externalUrl;       // Direct link to the simulated live post
    private String syncStatus;        // SYNCED, FAILED, PENDING

    private Integer viewsCount = 0;
    private Integer clicksCount = 0;
    private Integer applicantsCount = 0;

    private LocalDateTime publishedAt;

    public RecruitmentPostDistribution() {}

    public RecruitmentPostDistribution(RecruitmentPost post, String channelCode, String channelName,
                                       String externalListingId, String externalUrl, String syncStatus) {
        this.post = post;
        this.channelCode = channelCode;
        this.channelName = channelName;
        this.externalListingId = externalListingId;
        this.externalUrl = externalUrl;
        this.syncStatus = syncStatus;
        this.publishedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecruitmentPost getPost() { return post; }
    public void setPost(RecruitmentPost post) { this.post = post; }

    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getExternalListingId() { return externalListingId; }
    public void setExternalListingId(String externalListingId) { this.externalListingId = externalListingId; }

    public String getExternalUrl() { return externalUrl; }
    public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }

    public Integer getViewsCount() { return viewsCount; }
    public void setViewsCount(Integer viewsCount) { this.viewsCount = viewsCount; }

    public Integer getClicksCount() { return clicksCount; }
    public void setClicksCount(Integer clicksCount) { this.clicksCount = clicksCount; }

    public Integer getApplicantsCount() { return applicantsCount; }
    public void setApplicantsCount(Integer applicantsCount) { this.applicantsCount = applicantsCount; }

    public LocalDateTime getPublishedAt() { return publishedAt; }
    public void setPublishedAt(LocalDateTime publishedAt) { this.publishedAt = publishedAt; }
}
