package com.cadt.devices.model.coupon;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "coupon_usages", indexes = {
        @Index(name = "idx_coupon_usage_coupon", columnList = "couponId"),
        @Index(name = "idx_coupon_usage_user", columnList = "userId"),
        @Index(name = "idx_coupon_usage_order", columnList = "orderId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage extends BaseEntity {

    @Column(name = "coupon_id", nullable = false)
    private String couponId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "order_id")
    private String orderId; // Can be null for cart usage

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount;

    @Column(name = "order_amount", nullable = false)
    private BigDecimal orderAmount;
}
