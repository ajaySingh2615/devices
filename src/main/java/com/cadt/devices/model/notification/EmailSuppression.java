package com.cadt.devices.model.notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "email_suppressions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EmailSuppression {
    @Id
    @Column(length = 36)
    private String id;

    @Column(length = 191, nullable = false, unique = true)
    private String email;

    @Column(length = 64, nullable = false)
    private String reason; // bounce, complaint, manual

    @Column(length = 32, nullable = false)
    private String provider; // resend

    @Lob
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}


