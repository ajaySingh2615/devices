package com.cadt.devices.model.token;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name = "refresh_tokens", indexes = {@Index(name = "idx_rt_user", columnList = "userId"),
        @Index(name = "idx_rt_expires", columnList = "expiresAt")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Builder.Default
    private boolean revoked = false;

    private String ip;
    private String userAgent;

}
