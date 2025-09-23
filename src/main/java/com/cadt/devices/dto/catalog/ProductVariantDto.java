package com.cadt.devices.dto.catalog;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ProductVariantDto {

    private String id;
    private String productId;
    private String sku;
    private String mpn;
    private String color;
    private Integer storageGb;
    private Integer ramGb;
    private BigDecimal priceMrp;
    private BigDecimal priceSale;
    private BigDecimal taxRate;
    private int weightGrams;
    private boolean isActive;
    private Instant createdAt;

    // Inventory info
    private InventoryDto inventory;

    // CPU info
    private String cpuVendor; // enum name
    private String cpuSeries;
    private String cpuGeneration;
    private String cpuModel;

    // Operating system
    private String operatingSystem; // enum name
}
