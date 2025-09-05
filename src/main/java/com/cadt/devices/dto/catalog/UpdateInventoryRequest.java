package com.cadt.devices.dto.catalog;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateInventoryRequest {
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    @Min(value = 0, message = "Safety stock cannot be negative")
    private Integer safetyStock;

    @Min(value = 0, message = "Reserved cannot be negative")
    private Integer reserved;
}
