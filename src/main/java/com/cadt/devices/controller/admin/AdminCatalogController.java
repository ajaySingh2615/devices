package com.cadt.devices.controller.admin;

import com.cadt.devices.dto.catalog.*;
import com.cadt.devices.service.catalog.CatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    public AdminCatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
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
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("sortOrder"));
        // This would need a service method for admin view of all categories
        return ResponseEntity.ok().build(); // Placeholder
    }

    @GetMapping("/brands")
    public ResponseEntity<Page<BrandDto>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name"));
        // This would need a service method for admin view of all brands
        return ResponseEntity.ok().build(); // Placeholder
    }
}
