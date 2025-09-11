package com.cadt.devices.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderRequest {

    @NotBlank(message = "Address ID is required")
    private String addressId;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String couponCode;

    private String orderNotes;

    private String deliveryInstructions;

    // Razorpay payment details (if payment method is RAZORPAY)
    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String razorpaySignature;
}
