package com.cadt.devices.dto.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    private String id;

    private String variantId;

    private String title;

    private String sku;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal totalPrice;

    private BigDecimal taxRate;

    private BigDecimal taxAmount;

    private String productSnapshot;
}
