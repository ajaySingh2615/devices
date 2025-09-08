package com.cadt.devices.model.review;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "reviews", indexes = {
        @Index(name = "idx_review_user", columnList = "userId"),
        @Index(name = "idx_review_product", columnList = "productId"),
        @Index(name = "idx_review_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    @Column(nullable = false)
    private Integer rating;

    @NotBlank(message = "Title is required")
    @Column(length = 200, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.PENDING;

    // Helper methods
    public boolean isApproved() {
        return status == ReviewStatus.APPROVED;
    }

    public boolean isPending() {
        return status == ReviewStatus.PENDING;
    }

    public boolean isRejected() {
        return status == ReviewStatus.REJECTED;
    }
}
