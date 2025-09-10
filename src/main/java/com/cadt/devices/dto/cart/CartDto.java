package com.cadt.devices.dto.cart;

import com.cadt.devices.dto.coupon.CouponDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CartDto {
    private String id;
    private String userId;
    private String sessionId;
    private List<CartItemDto> items;
    private int totalItems;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    
    // Coupon information
    private CouponDto appliedCoupon;
    private BigDecimal couponDiscount;
    private BigDecimal finalTotal;
    
    private Instant createdAt;
    private Instant updatedAt;
}
