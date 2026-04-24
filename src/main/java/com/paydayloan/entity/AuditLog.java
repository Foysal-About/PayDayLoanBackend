package com.paydayloan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "PDL_AUDIT_LOG")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUDIT_ID")
    private Long auditId;

    @Column(name = "MODULE_NAME", nullable = false)
    private String moduleName;

    @Column(name = "ENTITY_NAME", nullable = false)
    private String entityName;

    @Column(name = "ENTITY_ID", nullable = false)
    private String entityId;

    @Column(name = "ACTION_TYPE", nullable = false)
    private String actionType;

    @Column(name = "ACTION_BY", nullable = false)
    private String actionBy;

    @Column(name = "ACTION_BY_TYPE", nullable = false)
    private String actionByType;

    @Column(name = "ACTION_AT", nullable = false, updatable = false)
    private LocalDateTime actionAt;

    @Column(name = "OLD_VALUE", columnDefinition = "CLOB")
    private String oldValue;

    @Column(name = "NEW_VALUE", columnDefinition = "CLOB")
    private String newValue;

    @Column(name = "IP_ADDRESS")
    private String ipAddress;

    @Column(name = "DEVICE_ID")
    private String deviceId;

    @Column(name = "SESSION_ID")
    private String sessionId;

    @Column(name = "REMARKS")
    private String remarks;

    @PrePersist
    protected void onCreate() {
        actionAt = LocalDateTime.now();
    }
}
