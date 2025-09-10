package com.cadt.devices.dto.coupon;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CouponApplicationResult {
    private boolean success;
    private String message;
    private CouponDto coupon;
    private BigDecimal discountAmount;
    private BigDecimal originalAmount;
    private BigDecimal finalAmount;
}
