package com.cadt.devices.service.admin;

import com.cadt.devices.dto.admin.DashboardStatsResponse;
import com.cadt.devices.dto.admin.SalesChartData;
import com.cadt.devices.dto.admin.TopProductData;
import com.cadt.devices.dto.admin.RecentActivityData;
import com.cadt.devices.repo.catalog.*;
import com.cadt.devices.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    /**
     * Get comprehensive dashboard statistics
     */
    public DashboardStatsResponse getDashboardStats() {
        // Basic counts
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByIsActiveTrue();
        long totalCategories = categoryRepository.count();
        long totalBrands = brandRepository.count();
        long totalUsers = userRepository.count();
        
        // Inventory stats
        long inStockItems = inventoryRepository.countInStockItems();
        long lowStockItems = inventoryRepository.findLowStockItems().size();
        long outOfStockItems = inventoryRepository.findOutOfStockItems().size();

        // Calculate total inventory value (simplified - using sale prices)
        BigDecimal totalInventoryValue = calculateTotalInventoryValue();

        // Growth metrics (last 30 days vs previous 30 days)
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LocalDateTime sixtyDaysAgo = LocalDateTime.now().minusDays(60);
        
        long recentProducts = productRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long previousProducts = productRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        double productGrowth = calculateGrowthPercentage(recentProducts, previousProducts);

        long recentUsers = userRepository.countByCreatedAtAfter(thirtyDaysAgo);
        long previousUsers = userRepository.countByCreatedAtBetween(sixtyDaysAgo, thirtyDaysAgo);
        double userGrowth = calculateGrowthPercentage(recentUsers, previousUsers);

        return DashboardStatsResponse.builder()
            .totalProducts(totalProducts)
            .activeProducts(activeProducts)
            .totalCategories(totalCategories)
            .totalBrands(totalBrands)
            .totalUsers(totalUsers)
            .inStockItems(inStockItems)
            .lowStockItems(lowStockItems)
            .outOfStockItems(outOfStockItems)
            .totalInventoryValue(totalInventoryValue)
            .productGrowthPercentage(productGrowth)
            .userGrowthPercentage(userGrowth)
            .build();
    }

    /**
     * Get sales chart data for the last 30 days
     */
    public List<SalesChartData> getSalesChartData(int days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        // Since we don't have orders yet, simulate with product creation data
        return generateMockSalesData(startDate, endDate);
    }

    /**
     * Get top performing products
     */
    public List<TopProductData> getTopProducts(int limit) {
        // Since we don't have sales data yet, return products with highest inventory value
        return productRepository.findAll(PageRequest.of(0, limit))
            .getContent()
            .stream()
            .map(product -> {
                BigDecimal totalValue = product.getVariants().stream()
                    .map(variant -> variant.getPriceSale())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                return TopProductData.builder()
                    .productId(product.getId())
                    .title(product.getTitle())
                    .totalRevenue(totalValue)
                    .unitsSold(0L) // Mock data
                    .averageRating(4.5) // Mock data
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Get recent activity logs
     */
    public List<RecentActivityData> getRecentActivity(int limit) {
        // Get recent products as activity
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        
        List<RecentActivityData> activities = new ArrayList<>();
        
        try {
            List<com.cadt.devices.model.catalog.Product> recentProducts = 
                productRepository.findByCreatedAtAfterOrderByCreatedAtDesc(oneDayAgo);
            
            activities = recentProducts.stream()
                .limit(limit)
                .map(product -> RecentActivityData.builder()
                    .id(product.getId())
                    .type("PRODUCT_CREATED")
                    .description("New product added: " + product.getTitle())
                    .timestamp(product.getCreatedAt())
                    .user("Admin") // Mock data
                    .build())
                .collect(Collectors.toList());
        } catch (Exception e) {
            // Return empty list if there's an issue
            activities = new ArrayList<>();
        }
        
        return activities;
    }

    /**
     * Get low stock alerts
     */
    public List<String> getLowStockAlerts() {
        return inventoryRepository.findLowStockItems()
            .stream()
            .map(inventory -> {
                // Get variant and product info
                return variantRepository.findById(inventory.getVariantId())
                    .map(variant -> productRepository.findById(variant.getProductId())
                        .map(product -> product.getTitle() + " (" + variant.getSku() + ") - " + 
                                       inventory.getAvailable() + " left")
                        .orElse("Unknown product - " + inventory.getAvailable() + " left"))
                    .orElse("Unknown variant - " + inventory.getAvailable() + " left");
            })
            .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalInventoryValue() {
        // This is a simplified calculation
        // In a real system, you'd join inventory with variants and calculate properly
        return inventoryRepository.findAll()
            .stream()
            .map(inventory -> {
                return variantRepository.findById(inventory.getVariantId())
                    .map(variant -> variant.getPriceSale().multiply(BigDecimal.valueOf(inventory.getQuantity())))
                    .orElse(BigDecimal.ZERO);
            })
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double calculateGrowthPercentage(long current, long previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((double) (current - previous) / previous) * 100.0;
    }

    private List<SalesChartData> generateMockSalesData(LocalDate startDate, LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1))
            .map(date -> SalesChartData.builder()
                .date(date)
                .revenue(BigDecimal.valueOf(Math.random() * 10000 + 5000)) // Mock revenue
                .orders((long) (Math.random() * 50 + 10)) // Mock orders
                .build())
            .collect(Collectors.toList());
    }
}
