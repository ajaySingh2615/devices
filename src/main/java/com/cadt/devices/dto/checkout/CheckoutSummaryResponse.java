package com.cadt.devices.dto.checkout;

import com.cadt.devices.dto.cart.CartItemDto;
import com.cadt.devices.dto.coupon.CouponDto;

import java.math.BigDecimal;
import java.util.List;

public record CheckoutSummaryResponse(
        List<CartItemDto> items,
        BigDecimal subtotal,
        BigDecimal shipping,
        BigDecimal tax,
        CouponDto appliedCoupon,
        BigDecimal discount,
        BigDecimal grandTotal
) {}


