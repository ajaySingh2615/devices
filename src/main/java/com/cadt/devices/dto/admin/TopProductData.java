package com.cadt.devices.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TopProductData {
    private String productId;
    private String title;
    private BigDecimal totalRevenue;
    private Long unitsSold;
    private Double averageRating;
}
