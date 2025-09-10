package com.cadt.devices.model.coupon;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "coupons", indexes = {
        @Index(name = "idx_coupon_code", columnList = "code", unique = true),
        @Index(name = "idx_coupon_active", columnList = "isActive"),
        @Index(name = "idx_coupon_dates", columnList = "startAt,endAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon extends BaseEntity {

    @NotBlank
    @Column(length = 50, nullable = false, unique = true)
    private String code;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal value;

    @DecimalMin(value = "0.0")
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;

    @DecimalMin(value = "0.0")
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;

    @NotNull
    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @NotNull
    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Min(0)
    @Column(name = "usage_limit", nullable = false)
    @Builder.Default
    private Integer usageLimit = 0; // 0 = unlimited

    @Min(0)
    @Column(name = "per_user_limit", nullable = false)
    @Builder.Default
    private Integer perUserLimit = 1;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // Helper methods
    public boolean isValid() {
        Instant now = Instant.now();
        return isActive && 
               now.isAfter(startAt) && 
               now.isBefore(endAt) &&
               (usageLimit == 0 || getUsageCount() < usageLimit);
    }

    public boolean isApplicable(BigDecimal orderAmount) {
        return isValid() && 
               (minOrderAmount == null || orderAmount.compareTo(minOrderAmount) >= 0);
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if (!isApplicable(orderAmount)) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount = BigDecimal.ZERO;
        
        if (type == CouponType.PERCENTAGE) {
            discount = orderAmount.multiply(value.divide(BigDecimal.valueOf(100)));
        } else if (type == CouponType.FIXED) {
            discount = value;
        }

        // Apply max discount limit if set
        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        // Don't exceed order amount
        if (discount.compareTo(orderAmount) > 0) {
            discount = orderAmount;
        }

        return discount;
    }

    // This would need to be implemented with a repository query
    private Integer getUsageCount() {
        // TODO: Implement with CouponUsageRepository
        return 0;
    }
}
