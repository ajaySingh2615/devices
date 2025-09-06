package com.cadt.devices.controller.admin;

import com.cadt.devices.dto.admin.*;
import com.cadt.devices.dto.catalog.*;
import com.cadt.devices.dto.user.UserResponse;
import com.cadt.devices.service.admin.AdminDashboardService;
import com.cadt.devices.service.catalog.CatalogService;
import com.cadt.devices.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCatalogController {

    private final CatalogService catalogService;
    private final AdminDashboardService dashboardService;
    private final UserService userService;

    public AdminCatalogController(CatalogService catalogService, AdminDashboardService dashboardService, UserService userService) {
        this.catalogService = catalogService;
        this.dashboardService = dashboardService;
        this.userService = userService;
    }

    // Dashboard Endpoints
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/dashboard/sales-chart")
    public ResponseEntity<List<SalesChartData>> getSalesChart(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getSalesChartData(days));
    }

    @GetMapping("/dashboard/top-products")
    public ResponseEntity<List<TopProductData>> getTopProducts(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getTopProducts(limit));
    }

    @GetMapping("/dashboard/recent-activity")
    public ResponseEntity<List<RecentActivityData>> getRecentActivity(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentActivity(limit));
    }

    @GetMapping("/dashboard/low-stock-alerts")
    public ResponseEntity<List<String>> getLowStockAlerts() {
        return ResponseEntity.ok(dashboardService.getLowStockAlerts());
    }

    // Categories Management
    @PostMapping("/categories")
    public ResponseEntity<CategoryDto> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(catalogService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable String id, 
                                                    @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(catalogService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
        catalogService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // Brands Management
    @PostMapping("/brands")
    public ResponseEntity<BrandDto> createBrand(@Valid @RequestBody CreateBrandRequest request) {
        return ResponseEntity.ok(catalogService.createBrand(request));
    }

    @PutMapping("/brands/{id}")
    public ResponseEntity<BrandDto> updateBrand(@PathVariable String id, 
                                              @Valid @RequestBody UpdateBrandRequest request) {
        return ResponseEntity.ok(catalogService.updateBrand(id, request));
    }

    // Products Management
    @PostMapping("/products")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(catalogService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable String id, 
                                                  @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(catalogService.updateProduct(id, request));
    }

    // Product Variants Management
    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ProductVariantDto> addVariant(@PathVariable String productId, 
                                                       @Valid @RequestBody CreateVariantRequest request) {
        return ResponseEntity.ok(catalogService.addVariant(productId, request));
    }

    @PutMapping("/variants/{id}")
    public ResponseEntity<ProductVariantDto> updateVariant(@PathVariable String id, 
                                                         @Valid @RequestBody UpdateVariantRequest request) {
        return ResponseEntity.ok(catalogService.updateVariant(id, request));
    }

    // Inventory Management
    @PutMapping("/inventory/{variantId}")
    public ResponseEntity<InventoryDto> updateInventory(@PathVariable String variantId, 
                                                      @Valid @RequestBody UpdateInventoryRequest request) {
        return ResponseEntity.ok(catalogService.updateInventory(variantId, request));
    }

    // Admin-only catalog listings
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        // Return all categories (including inactive) for admin view
        return ResponseEntity.ok(catalogService.getAllCategories());
    }

    @GetMapping("/brands")
    public ResponseEntity<List<BrandDto>> getAllBrands() {
        // Return all brands (including inactive) for admin view
        return ResponseEntity.ok(catalogService.getAllBrands());
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        // Return all products (including inactive) for admin view
        return ResponseEntity.ok(catalogService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String id) {
        // Return product by ID for admin view
        return ResponseEntity.ok(catalogService.getProductById(id));
    }

    // User Management Endpoints
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<UserResponse> updateUserRole(@PathVariable String userId, @RequestBody UpdateUserRoleRequest request) {
        return ResponseEntity.ok(userService.updateUserRole(userId, request.getRole()));
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<UserResponse> updateUserStatus(@PathVariable String userId, @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, request.getStatus()));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }

    // Request DTOs for user management
    public static class UpdateUserRoleRequest {
        private String role;
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    public static class UpdateUserStatusRequest {
        private String status;
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
