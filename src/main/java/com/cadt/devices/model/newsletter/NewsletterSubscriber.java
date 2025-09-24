package com.cadt.devices.model.newsletter;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "newsletter_subscribers",
        indexes = {
                @Index(name = "uq_newsletter_email_idx", columnList = "email", unique = false)
        })
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterSubscriber {
    @Id
    private String id;

    @PrePersist
    public void prePersist() {
        if (id == null) id = UUID.randomUUID().toString();
    }

    @Column(nullable = false, length = 191)
    private String email;

    @Column(length = 64)
    private String source;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}


