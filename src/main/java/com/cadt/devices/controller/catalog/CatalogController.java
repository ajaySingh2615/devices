package com.cadt.devices.controller.catalog;

import com.cadt.devices.dto.catalog.BrandDto;
import com.cadt.devices.dto.catalog.CategoryDto;
import com.cadt.devices.dto.catalog.ProductDto;
import com.cadt.devices.service.catalog.CatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // categories
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getCategories() {
        return ResponseEntity.ok(catalogService.getAllActiveCategories());
    }

    @GetMapping("/categories/tree")
    public ResponseEntity<List<CategoryDto>> getCategoryTree() {
        return ResponseEntity.ok(catalogService.getCategoryTree());
    }

    @GetMapping("/categories/{slug}")
    public ResponseEntity<CategoryDto> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(catalogService.getCategoryBySlug(slug));
    }

    // Brands
    @GetMapping("/brands")
    public ResponseEntity<List<BrandDto>> getBrands() {
        return ResponseEntity.ok(catalogService.getAllActiveBrands());
    }

    @GetMapping("/brands/{slug}")
    public ResponseEntity<BrandDto> getBrandBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(catalogService.getBrandBySlug(slug));
    }

    // Products
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String condition,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false, name = "processorVendor") String processorVendor,
            @RequestParam(required = false, name = "processorSeries") String processorSeries,
            @RequestParam(required = false, name = "processorGeneration") String processorGeneration) {

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);
        Pageable pageable = PageRequest.of(page, size, sortDirection, sort);

        return ResponseEntity.ok(catalogService.searchProducts(
                q, category, brand, condition, minPrice, maxPrice,
                processorVendor, processorSeries, processorGeneration,
                pageable));
    }

    @GetMapping("/products/{slug}")
    public ResponseEntity<ProductDto> getProductBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(catalogService.getProductBySlug(slug));
    }
}
