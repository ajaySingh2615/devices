package com.cadt.devices.model.audit;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "audit_logs", indexes = {@Index(name = "idx_audit_user", columnList = "userId"),
        @Index(name = "idx_audit_action", columnList = "action")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    private String userId;

    @Column(length = 64, nullable = false)
    private String action;

    @Lob
    private String metaJson;
    private String ip;
}
