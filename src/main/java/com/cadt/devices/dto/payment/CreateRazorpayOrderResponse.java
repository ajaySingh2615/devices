package com.cadt.devices.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRazorpayOrderResponse {
    private String id;
    private String entity;
    private Long amount;
    private String amountPaid;
    private String amountDue;
    private String currency;
    private String receipt;
    private String status;
    private Long attempts;
    private String notes;
    private Long createdAt;
}
