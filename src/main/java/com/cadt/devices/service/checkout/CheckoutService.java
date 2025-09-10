package com.cadt.devices.service.checkout;

import com.cadt.devices.dto.cart.CartDto;
import com.cadt.devices.dto.checkout.CheckoutSummaryRequest;
import com.cadt.devices.dto.checkout.CheckoutSummaryResponse;
import com.cadt.devices.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CheckoutService {
    private final CartService carts;

    @Transactional(readOnly = true)
    public CheckoutSummaryResponse summarize(String userId, String sessionId, CheckoutSummaryRequest r) {
        CartDto cart = carts.getOrCreateCart(userId, sessionId);

        BigDecimal shipping = estimateShipping(cart);
        BigDecimal tax = cart.getTaxTotal() != null ? cart.getTaxTotal() : BigDecimal.ZERO;
        BigDecimal discount = cart.getCouponDiscount() != null ? cart.getCouponDiscount() : BigDecimal.ZERO;
        BigDecimal subtotal = cart.getSubtotal() != null ? cart.getSubtotal() : BigDecimal.ZERO;

        BigDecimal grand = subtotal.add(shipping).add(tax).subtract(discount);

        return new CheckoutSummaryResponse(
                cart.getItems(),
                subtotal,
                shipping,
                tax,
                cart.getAppliedCoupon(),
                discount,
                grand.max(BigDecimal.ZERO)
        );
    }

    private BigDecimal estimateShipping(CartDto cart) {
        // Flat-rate example; can be replaced with rules or courier API
        if (cart.getSubtotal() != null && cart.getSubtotal().compareTo(new BigDecimal("999")) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("49");
    }
}


