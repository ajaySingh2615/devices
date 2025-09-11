package com.cadt.devices.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceOrderResponse {

    private String orderId;

    private String message;

    private boolean success;

    private String status;

    private String paymentStatus;
}
