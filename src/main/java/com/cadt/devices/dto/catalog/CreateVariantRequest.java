package com.cadt.devices.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateVariantRequest {
    @NotBlank(message = "SKU is required")
    private String sku;

    private String mpn;
    private String color;
    private Integer storageGb;
    private Integer ramGb;

    @NotNull(message = "MRP is required")
    private BigDecimal priceMrp;

    @NotNull(message = "Sale price is required")
    private BigDecimal priceSale;

    private BigDecimal taxRate;
    private Integer weightGrams;

    // CPU fields
    private String cpuVendor; // INTEL, AMD, APPLE
    private String cpuSeries; // i5, Ryzen 5, M1
    private String cpuGeneration; // 12th Gen
    private String cpuModel; // i7-1165G7
}
