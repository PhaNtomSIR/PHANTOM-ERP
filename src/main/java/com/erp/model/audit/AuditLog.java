package com.erp.model.audit;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModuleName module;

    @Column(nullable = false)
    private String action;

    private String entityId;
    private String performedBy;
    private String details;

    public enum ModuleName {
        FINANCE, HR, PROCUREMENT, SALES, MANUFACTURING, INVENTORY, SYSTEM, ADMIN, AUTH
    }

    public AuditLog() {}

    public AuditLog(ModuleName module, String action, String entityId, String performedBy, String details) {
        this.timestamp = LocalDateTime.now();
        this.module = module;
        this.action = action;
        this.entityId = entityId;
        this.performedBy = performedBy;
        this.details = details;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public ModuleName getModule() { return module; }
    public void setModule(ModuleName module) { this.module = module; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityId() { return entityId; }
    public void setEntityId(String entityId) { this.entityId = entityId; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
