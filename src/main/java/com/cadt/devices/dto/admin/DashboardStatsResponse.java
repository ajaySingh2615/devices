package com.cadt.devices.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardStatsResponse {
    private long totalProducts;
    private long activeProducts;
    private long totalCategories;
    private long totalBrands;
    private long totalUsers;
    
    private long inStockItems;
    private long lowStockItems;
    private long outOfStockItems;
    
    private BigDecimal totalInventoryValue;
    private double productGrowthPercentage;
    private double userGrowthPercentage;
}
