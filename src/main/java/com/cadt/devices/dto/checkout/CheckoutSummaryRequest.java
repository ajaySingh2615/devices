package com.cadt.devices.dto.checkout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CheckoutSummaryRequest(
        @NotBlank String addressId,
        String couponCode,
        @NotNull String paymentMethod
) {}


