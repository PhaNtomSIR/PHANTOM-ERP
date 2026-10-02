package com.erp.service;

import com.erp.model.audit.AuditLog;
import com.erp.model.hr.*;
import com.erp.repository.AuditLogRepository;
import com.erp.repository.DepartmentRepository;
import com.erp.repository.JobRequisitionRepository;
import com.erp.repository.RecruitmentChannelRepository;
import com.erp.repository.RecruitmentPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class RecruitmentService {

    private final RecruitmentChannelRepository channelRepo;
    private final RecruitmentPostRepository postRepo;
    private final JobRequisitionRepository requisitionRepo;
    private final DepartmentRepository departmentRepo;
    private final AuditLogRepository auditRepo;

    public RecruitmentService(RecruitmentChannelRepository channelRepo,
                              RecruitmentPostRepository postRepo,
                              JobRequisitionRepository requisitionRepo,
                              DepartmentRepository departmentRepo,
                              AuditLogRepository auditRepo) {
        this.channelRepo = channelRepo;
        this.postRepo = postRepo;
        this.requisitionRepo = requisitionRepo;
        this.departmentRepo = departmentRepo;
        this.auditRepo = auditRepo;
    }

    public List<RecruitmentChannel> getAllChannels() {
        return channelRepo.findAll();
    }

    public RecruitmentChannel saveChannel(RecruitmentChannel channel) {
        return channelRepo.save(channel);
    }

    public RecruitmentChannel toggleChannel(Long id, Boolean connected, Boolean autoPost) {
        RecruitmentChannel ch = channelRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recruitment channel not found: " + id));
        if (connected != null) ch.setConnected(connected);
        if (autoPost != null) ch.setAutoPostEnabled(autoPost);
        ch.setLastSyncTime(LocalDateTime.now());
        RecruitmentChannel saved = channelRepo.save(ch);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR, "CHANNEL_CONFIG_UPDATE",
                ch.getChannelCode(), "HR Administrator",
                "Channel " + ch.getName() + " updated (Connected: " + ch.isConnected() + ", Auto-Post: " + ch.isAutoPostEnabled() + ")"));

        return saved;
    }

    public List<RecruitmentPost> getAllPosts() {
        return postRepo.findAllByOrderByPostedDateDesc();
    }

    public RecruitmentPost getPostById(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recruitment post not found: " + id));
    }

    @Transactional
    public RecruitmentPost createAndBroadcastPost(RecruitmentPost post, List<String> targetChannelCodes) {
        boolean isNew = post.getId() == null;
        if (post.getPostingCode() == null || post.getPostingCode().isBlank()) {
            long count = postRepo.count() + 1;
            post.setPostingCode(String.format("POST-2026-%03d", count));
        }
        if (post.getPostedDate() == null) {
            post.setPostedDate(LocalDate.now());
        }
        if (post.getStatus() == null) {
            post.setStatus(RecruitmentPost.PostStatus.PUBLISHED);
        }

        // Resolve Department
        if (post.getDepartment() != null && post.getDepartment().getId() != null) {
            departmentRepo.findById(post.getDepartment().getId()).ifPresent(post::setDepartment);
        } else if (post.getJobRequisition() != null && post.getJobRequisition().getId() != null) {
            requisitionRepo.findById(post.getJobRequisition().getId()).ifPresent(req -> {
                if (req.getDepartment() != null) post.setDepartment(req.getDepartment());
            });
        }
        if (post.getDepartment() == null) {
            departmentRepo.findAll().stream().findFirst().ifPresent(post::setDepartment);
        }

        // Determine target channels
        List<String> activeChannelCodes = targetChannelCodes;
        if (activeChannelCodes == null || activeChannelCodes.isEmpty()) {
            activeChannelCodes = channelRepo.findByConnectedTrue().stream()
                    .filter(RecruitmentChannel::isAutoPostEnabled)
                    .map(RecruitmentChannel::getChannelCode)
                    .toList();
        }

        Random rand = new Random();
        List<String> broadcastedNames = new ArrayList<>();

        for (String code : activeChannelCodes) {
            Optional<RecruitmentChannel> chOpt = channelRepo.findByChannelCode(code);
            String channelName = code;
            String listingId;
            String publicUrl;

            int randomNum = 10000 + rand.nextInt(90000);

            switch (code.toUpperCase()) {
                case "LINKEDIN":
                    channelName = "LinkedIn Talent Solutions";
                    listingId = "LI-JOB-" + randomNum;
                    publicUrl = "https://www.linkedin.com/jobs/view/phantom-erp-" + randomNum;
                    break;
                case "BDJOBS":
                    channelName = "BDJobs Employer Network";
                    listingId = "BDJ-CIRCULAR-" + randomNum;
                    publicUrl = "https://jobs.bdjobs.com/jobdetails.asp?id=" + randomNum;
                    break;
                case "INDEED":
                    channelName = "Indeed Recruiter";
                    listingId = "IND-POST-" + randomNum;
                    publicUrl = "https://www.indeed.com/viewjob?jk=erp" + randomNum;
                    break;
                case "CAREERS_PORTAL":
                default:
                    channelName = "PHANTOM-ERP Careers Portal";
                    listingId = "PHANTOM-CAR-" + randomNum;
                    publicUrl = "https://careers.phantom-erp.cloud/jobs/" + post.getPostingCode();
                    break;
            }

            RecruitmentPostDistribution dist = new RecruitmentPostDistribution(
                    post, code, channelName, listingId, publicUrl, "SYNCED"
            );
            post.addDistribution(dist);
            broadcastedNames.add(channelName);

            // Update channel stats
            if (chOpt.isPresent()) {
                RecruitmentChannel ch = chOpt.get();
                ch.setActivePostsCount(ch.getActivePostsCount() + 1);
                ch.setLastSyncTime(LocalDateTime.now());
                channelRepo.save(ch);
            }
        }

        // If linked to an approved Job Requisition, mark the requisition as OPEN (actively recruiting)
        if (post.getJobRequisition() != null && post.getJobRequisition().getId() != null) {
            requisitionRepo.findById(post.getJobRequisition().getId()).ifPresent(req -> {
                req.setStatus(JobRequisition.RequisitionStatus.OPEN);
                requisitionRepo.save(req);
            });
        }

        RecruitmentPost saved = postRepo.save(post);

        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR,
                isNew ? "BROADCAST_RECRUITMENT_POST" : "UPDATE_RECRUITMENT_POST",
                saved.getPostingCode(), "HR Talent Acquisition Team",
                "Job post '" + saved.getJobTitle() + "' broadcasted automatically to: " + String.join(", ", broadcastedNames)));

        return saved;
    }

    @Transactional
    public RecruitmentPost closePost(Long id) {
        RecruitmentPost post = getPostById(id);
        post.setStatus(RecruitmentPost.PostStatus.CLOSED);
        RecruitmentPost saved = postRepo.save(post);

        // Adjust active count in channels
        for (RecruitmentPostDistribution dist : saved.getDistributions()) {
            channelRepo.findByChannelCode(dist.getChannelCode()).ifPresent(ch -> {
                if (ch.getActivePostsCount() > 0) {
                    ch.setActivePostsCount(ch.getActivePostsCount() - 1);
                    channelRepo.save(ch);
                }
            });
        }

        auditRepo.save(new AuditLog(AuditLog.ModuleName.HR, "CLOSE_RECRUITMENT_POST",
                saved.getPostingCode(), "HR Director",
                "Recruitment post '" + saved.getJobTitle() + "' closed across all connected platforms."));

        return saved;
    }

    @Transactional
    public RecruitmentPost simulateEngagement(Long postId, String channelCode, int applicantsToAdd) {
        RecruitmentPost post = getPostById(postId);
        for (RecruitmentPostDistribution dist : post.getDistributions()) {
            if (channelCode == null || dist.getChannelCode().equalsIgnoreCase(channelCode)) {
                dist.setViewsCount(dist.getViewsCount() + applicantsToAdd * 14);
                dist.setClicksCount(dist.getClicksCount() + applicantsToAdd * 5);
                dist.setApplicantsCount(dist.getApplicantsCount() + applicantsToAdd);

                channelRepo.findByChannelCode(dist.getChannelCode()).ifPresent(ch -> {
                    ch.setTotalApplicantsReceived(ch.getTotalApplicantsReceived() + applicantsToAdd);
                    channelRepo.save(ch);
                });
            }
        }
        return postRepo.save(post);
    }
}
