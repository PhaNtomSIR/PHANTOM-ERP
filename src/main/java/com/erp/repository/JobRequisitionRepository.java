package com.erp.repository;

import com.erp.model.hr.JobRequisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobRequisitionRepository extends JpaRepository<JobRequisition, Long> {
    Optional<JobRequisition> findByRequisitionNumber(String requisitionNumber);
    List<JobRequisition> findByStatus(JobRequisition.RequisitionStatus status);
    List<JobRequisition> findByDepartmentId(Long departmentId);
}
