package com.cadt.devices.dto.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CartDto {
    private String id;
    private String userId;
    private String sessionId;
    private List<CartItemDto> items;
    private int totalItems;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal grandTotal;
    private Instant createdAt;
    private Instant updatedAt;
}
