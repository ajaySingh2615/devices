package com.cadt.devices.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponUsageStatsDto {
    private String couponId;
    private String couponCode;
    private String couponName;
    private Integer totalUsage;
    private Integer usageLimit;
    private BigDecimal totalDiscountGiven;
    private BigDecimal averageOrderValue;
    private Instant firstUsed;
    private Instant lastUsed;
    private Integer uniqueUsers;
    private Double usageRate; // percentage of limit used
}
