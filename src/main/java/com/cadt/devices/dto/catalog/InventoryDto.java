package com.cadt.devices.dto.catalog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryDto {
    private String variantId;
    private int quantity;
    private int safetyStock;
    private int reserved;
    private int available;
    private boolean inStock;
    private boolean lowStock;
}
