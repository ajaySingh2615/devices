package com.cadt.devices.dto.order;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequest {
    @NotBlank
    private String status; // CREATED, PAID, PACKED, SHIPPED, DELIVERED, COMPLETED, CANCELLED, RETURNED
}


