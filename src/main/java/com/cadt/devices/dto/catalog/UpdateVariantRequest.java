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
}
