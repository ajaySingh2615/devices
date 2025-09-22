package com.cadt.devices.model.catalog;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "product_variants", indexes = {
        @Index(name = "idx_variant_sku", columnList = "sku", unique = true),
        @Index(name = "idx_variant_product", columnList = "productId"),
        @Index(name = "idx_variant_price", columnList = "priceSale"),
        @Index(name = "idx_variant_cpu_vendor", columnList = "cpuVendor"),
        @Index(name = "idx_variant_cpu_series", columnList = "cpuSeries")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant extends BaseEntity {

    @Column(nullable = false)
    private String productId;

    @NotBlank
    @Column(length = 100, nullable = false, unique = true)
    private String sku;

    @Column(length = 100)
    private String mpn; // Manufacturer Part Number

    @Column(length = 50)
    private String color;

    private Integer storageGb;
    private Integer ramGb;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceMrp;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal priceSale;

    @Column(nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxRate = new BigDecimal("18.00");

    @Column(nullable = false)
    @Builder.Default
    private int weightGrams = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    // CPU fields
    private ProcessorVendor cpuVendor; // INTEL, AMD, APPLE

    @Column(length = 50)
    private String cpuSeries; // e.g., i5, i7, Ryzen 5, M1

    @Column(length = 50)
    private String cpuGeneration; // e.g., 10th Gen, 12th Gen

    @Column(length = 80)
    private String cpuModel; // e.g., i7-1165G7, Ryzen 5 5600U
}
