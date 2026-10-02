package com.erp.repository;

import com.erp.model.hr.RecruitmentChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecruitmentChannelRepository extends JpaRepository<RecruitmentChannel, Long> {
    Optional<RecruitmentChannel> findByChannelCode(String channelCode);
    List<RecruitmentChannel> findByConnectedTrue();
}
