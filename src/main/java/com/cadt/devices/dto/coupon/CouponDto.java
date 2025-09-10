package com.cadt.devices.dto.coupon;

import com.cadt.devices.model.coupon.CouponType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CouponDto {
    private String id;
    private String code;
    private String name;
    private String description;
    private CouponType type;
    private BigDecimal value;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Instant startAt;
    private Instant endAt;
    private Integer usageLimit;
    private Integer perUserLimit;
    private boolean isActive;
    private Instant createdAt;
}
