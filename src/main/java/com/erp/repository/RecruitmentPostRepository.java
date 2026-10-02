package com.erp.repository;

import com.erp.model.hr.RecruitmentPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentPostRepository extends JpaRepository<RecruitmentPost, Long> {
    Optional<RecruitmentPost> findByPostingCode(String postingCode);
    List<RecruitmentPost> findByStatus(RecruitmentPost.PostStatus status);
    List<RecruitmentPost> findAllByOrderByPostedDateDesc();
}
