package com.erp.repository;

import com.erp.model.audit.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop100ByOrderByTimestampDesc();
    List<AuditLog> findTop15ByOrderByTimestampDesc();
    List<AuditLog> findByModuleOrderByTimestampDesc(AuditLog.ModuleName module);
}
