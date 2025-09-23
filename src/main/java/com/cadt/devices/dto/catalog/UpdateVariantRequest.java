package com.cadt.devices.dto.catalog;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateVariantRequest {
    private String sku;
    private String mpn;
    private String color;
    private Integer storageGb;
    private Integer ramGb;
    private BigDecimal priceMrp;
    private BigDecimal priceSale;
    private BigDecimal taxRate;
    private Integer weightGrams;
    private Boolean isActive;

    // CPU fields
    private String cpuVendor;
    private String cpuSeries;
    private String cpuGeneration;
    private String cpuModel;

    // Operating system
    private String operatingSystem;

    // Features
    private Boolean touchscreen;
    private String useCase;
}
