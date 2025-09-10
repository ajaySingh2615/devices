package com.cadt.devices.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyPaymentResponse {
    private boolean verified;
    private String message;
    private String orderId;
}
