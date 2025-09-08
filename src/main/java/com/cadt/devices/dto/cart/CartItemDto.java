package com.cadt.devices.dto.cart;

import com.cadt.devices.dto.catalog.ProductVariantDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CartItemDto {
    private String id;
    private String cartId;
    private String variantId;
    private Integer quantity;
    private BigDecimal priceSnapshot;
    private BigDecimal taxRateSnapshot;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal total;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Related data
    private ProductVariantDto variant;
}
