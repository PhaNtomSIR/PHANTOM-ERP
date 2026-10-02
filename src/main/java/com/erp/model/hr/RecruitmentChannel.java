package com.erp.model.hr;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "recruitment_channels")
public class RecruitmentChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String channelCode; // LINKEDIN, BDJOBS, INDEED, CAREERS_PORTAL

    @Column(nullable = false)
    private String name;

    private String platformType; // PROFESSIONAL_NETWORK, REGIONAL_PORTAL, GLOBAL_BOARD, CORPORATE_WEBSITE
    private String logoIcon;     // e.g. 💼, 🇧🇩, 🔍, 🌐
    private String accountIdentifier; // e.g. "OmniERP Talent Solutions ID #892", "BDJobs Corporate Acct: 94021"

    private boolean connected = true;
    private boolean autoPostEnabled = true;

    private String apiEndpoint;
    private String apiKeyPreview; // masked API key preview

    private Integer activePostsCount = 0;
    private Integer totalApplicantsReceived = 0;
    private LocalDateTime lastSyncTime;

    public RecruitmentChannel() {}

    public RecruitmentChannel(String channelCode, String name, String platformType, String logoIcon,
                              String accountIdentifier, boolean connected, boolean autoPostEnabled,
                              String apiEndpoint, String apiKeyPreview) {
        this.channelCode = channelCode;
        this.name = name;
        this.platformType = platformType;
        this.logoIcon = logoIcon;
        this.accountIdentifier = accountIdentifier;
        this.connected = connected;
        this.autoPostEnabled = autoPostEnabled;
        this.apiEndpoint = apiEndpoint;
        this.apiKeyPreview = apiKeyPreview;
        this.lastSyncTime = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPlatformType() { return platformType; }
    public void setPlatformType(String platformType) { this.platformType = platformType; }

    public String getLogoIcon() { return logoIcon; }
    public void setLogoIcon(String logoIcon) { this.logoIcon = logoIcon; }

    public String getAccountIdentifier() { return accountIdentifier; }
    public void setAccountIdentifier(String accountIdentifier) { this.accountIdentifier = accountIdentifier; }

    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }

    public boolean isAutoPostEnabled() { return autoPostEnabled; }
    public void setAutoPostEnabled(boolean autoPostEnabled) { this.autoPostEnabled = autoPostEnabled; }

    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }

    public String getApiKeyPreview() { return apiKeyPreview; }
    public void setApiKeyPreview(String apiKeyPreview) { this.apiKeyPreview = apiKeyPreview; }

    public Integer getActivePostsCount() { return activePostsCount; }
    public void setActivePostsCount(Integer activePostsCount) { this.activePostsCount = activePostsCount; }

    public Integer getTotalApplicantsReceived() { return totalApplicantsReceived; }
    public void setTotalApplicantsReceived(Integer totalApplicantsReceived) { this.totalApplicantsReceived = totalApplicantsReceived; }

    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
}
