package com.cadt.devices.service.catalog;

import com.cadt.devices.dto.catalog.*;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.catalog.*;
import com.cadt.devices.repo.catalog.*;
import com.cadt.devices.repo.media.MediaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {
    
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final ProductRepository productRepo;
    private final ProductVariantRepository variantRepo;
    private final InventoryRepository inventoryRepo;
    private final MediaRepository mediaRepo;

    @Transactional
    public CategoryDto updateCategory(String id, UpdateCategoryRequest request) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("CATEGORY_NOT_FOUND", "Category not found"));

        if (request.getName() != null) category.setName(request.getName());
        if (request.getSlug() != null) {
            if (!category.getSlug().equals(request.getSlug()) && categoryRepo.existsBySlug(request.getSlug())) {
                throw new ApiException("SLUG_EXISTS", "Category slug already exists");
            }
            category.setSlug(request.getSlug());
        }
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIsActive() != null) category.setActive(request.getIsActive());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getParentId() != null) category.setParentId(request.getParentId());

        return toCategoryDto(categoryRepo.save(category));
    }

    @Transactional
    public void deleteCategory(String id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("CATEGORY_NOT_FOUND", "Category not found"));

        // Check if category has products
        if (productRepo.existsByCategoryId(id)) {
            throw new ApiException("CATEGORY_HAS_PRODUCTS", "Cannot delete category with products");
        }

        categoryRepo.delete(category);
    }

    @Transactional
    public BrandDto updateBrand(String id, UpdateBrandRequest request) {
        Brand brand = brandRepo.findById(id)
                .orElseThrow(() -> new ApiException("BRAND_NOT_FOUND", "Brand not found"));

        if (request.getName() != null) brand.setName(request.getName());
        if (request.getSlug() != null) {
            if (!brand.getSlug().equals(request.getSlug()) && brandRepo.existsBySlug(request.getSlug())) {
                throw new ApiException("SLUG_EXISTS", "Brand slug already exists");
            }
            brand.setSlug(request.getSlug());
        }
        if (request.getDescription() != null) brand.setDescription(request.getDescription());
        if (request.getLogoUrl() != null) brand.setLogoUrl(request.getLogoUrl());
        if (request.getIsActive() != null) brand.setActive(request.getIsActive());

        return toBrandDto(brandRepo.save(brand));
    }

    @Transactional
    public ProductDto createProduct(CreateProductRequest request) {
        if (productRepo.existsBySlug(request.getSlug())) {
            throw new ApiException("SLUG_EXISTS", "Product slug already exists");
        }

        // Verify category and brand exist
        if (!categoryRepo.existsById(request.getCategoryId())) {
            throw new ApiException("CATEGORY_NOT_FOUND", "Category not found");
        }
        if (!brandRepo.existsById(request.getBrandId())) {
            throw new ApiException("BRAND_NOT_FOUND", "Brand not found");
        }

        Product product = Product.builder()
                .categoryId(request.getCategoryId())
                .brandId(request.getBrandId())
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .conditionGrade(request.getConditionGrade())
                .warrantyMonths(request.getWarrantyMonths())
                .build();

        return toProductDto(productRepo.save(product));
    }

    @Transactional
    public ProductDto updateProduct(String id, UpdateProductRequest request) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ApiException("PRODUCT_NOT_FOUND", "Product not found"));

        if (request.getCategoryId() != null) {
            if (!categoryRepo.existsById(request.getCategoryId())) {
                throw new ApiException("CATEGORY_NOT_FOUND", "Category not found");
            }
            product.setCategoryId(request.getCategoryId());
        }
        if (request.getBrandId() != null) {
            if (!brandRepo.existsById(request.getBrandId())) {
                throw new ApiException("BRAND_NOT_FOUND", "Brand not found");
            }
            product.setBrandId(request.getBrandId());
        }
        if (request.getTitle() != null) product.setTitle(request.getTitle());
        if (request.getSlug() != null) {
            if (!product.getSlug().equals(request.getSlug()) && productRepo.existsBySlug(request.getSlug())) {
                throw new ApiException("SLUG_EXISTS", "Product slug already exists");
            }
            product.setSlug(request.getSlug());
        }
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getConditionGrade() != null) product.setConditionGrade(request.getConditionGrade());
        if (request.getWarrantyMonths() != null) product.setWarrantyMonths(request.getWarrantyMonths());
        if (request.getIsActive() != null) product.setActive(request.getIsActive());

        return toProductDto(productRepo.save(product));
    }

    @Transactional
    public ProductVariantDto addVariant(String productId, CreateVariantRequest request) {
        if (!productRepo.existsById(productId)) {
            throw new ApiException("PRODUCT_NOT_FOUND", "Product not found");
        }
        if (variantRepo.existsBySku(request.getSku())) {
            throw new ApiException("SKU_EXISTS", "SKU already exists");
        }

        ProductVariant variant = ProductVariant.builder()
                .productId(productId)
                .sku(request.getSku())
                .mpn(request.getMpn())
                .color(request.getColor())
                .storageGb(request.getStorageGb())
                .ramGb(request.getRamGb())
                .priceMrp(request.getPriceMrp())
                .priceSale(request.getPriceSale())
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : new BigDecimal("18.00"))
                .weightGrams(request.getWeightGrams() != null ? request.getWeightGrams() : 0)
                .build();

        variant = variantRepo.save(variant);

        // Create inventory record
        Inventory inventory = Inventory.builder()
                .variantId(variant.getId())
                .quantity(0)
                .safetyStock(5)
                .reserved(0)
                .build();
        inventoryRepo.save(inventory);

        return toVariantDto(variant);
    }

    @Transactional
    public ProductVariantDto updateVariant(String id, UpdateVariantRequest request) {
        ProductVariant variant = variantRepo.findById(id)
                .orElseThrow(() -> new ApiException("VARIANT_NOT_FOUND", "Variant not found"));

        if (request.getSku() != null) {
            if (!variant.getSku().equals(request.getSku()) && variantRepo.existsBySku(request.getSku())) {
                throw new ApiException("SKU_EXISTS", "SKU already exists");
            }
            variant.setSku(request.getSku());
        }
        if (request.getMpn() != null) variant.setMpn(request.getMpn());
        if (request.getColor() != null) variant.setColor(request.getColor());
        if (request.getStorageGb() != null) variant.setStorageGb(request.getStorageGb());
        if (request.getRamGb() != null) variant.setRamGb(request.getRamGb());
        if (request.getPriceMrp() != null) variant.setPriceMrp(request.getPriceMrp());
        if (request.getPriceSale() != null) variant.setPriceSale(request.getPriceSale());
        if (request.getTaxRate() != null) variant.setTaxRate(request.getTaxRate());
        if (request.getWeightGrams() != null) variant.setWeightGrams(request.getWeightGrams());
        if (request.getIsActive() != null) variant.setActive(request.getIsActive());

        return toVariantDto(variantRepo.save(variant));
    }

    @Transactional
    public InventoryDto updateInventory(String variantId, UpdateInventoryRequest request) {
        Inventory inventory = inventoryRepo.findByVariantId(variantId)
                .orElseThrow(() -> new ApiException("INVENTORY_NOT_FOUND", "Inventory not found"));

        if (request.getQuantity() != null) inventory.setQuantity(request.getQuantity());
        if (request.getSafetyStock() != null) inventory.setSafetyStock(request.getSafetyStock());
        if (request.getReserved() != null) inventory.setReserved(request.getReserved());

        inventory = inventoryRepo.save(inventory);

        return InventoryDto.builder()
                .variantId(inventory.getVariantId())
                .quantity(inventory.getQuantity())
                .safetyStock(inventory.getSafetyStock())
                .reserved(inventory.getReserved())
                .available(inventory.getAvailable())
                .inStock(inventory.isInStock())
                .lowStock(inventory.isLowStock())
                .build();
    }
    
    // Additional core service methods
    public List<CategoryDto> getAllActiveCategories() {
        return categoryRepo.findAllActiveOrderBySortOrder()
                .stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }
    
    // Admin method to get ALL categories (including inactive)
    public List<CategoryDto> getAllCategories() {
        return categoryRepo.findAllByOrderBySortOrder()
                .stream()
                .map(this::toCategoryDto)
                .collect(Collectors.toList());
    }
    
    public List<CategoryDto> getCategoryTree() {
        List<Category> rootCategories = categoryRepo.findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();
        return rootCategories.stream()
                .map(this::toCategoryDtoWithChildren)
                .collect(Collectors.toList());
    }
    
    public CategoryDto getCategoryBySlug(String slug) {
        Category category = categoryRepo.findBySlug(slug)
                .orElseThrow(() -> new ApiException("CATEGORY_NOT_FOUND", "Category not found"));
        return toCategoryDto(category);
    }
    
    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest request) {
        if (categoryRepo.existsBySlug(request.getSlug())) {
            throw new ApiException("SLUG_EXISTS", "Category slug already exists");
        }
        
        Category category = Category.builder()
                .parentId(request.getParentId())
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .sortOrder(request.getSortOrder())
                .build();
                
        return toCategoryDto(categoryRepo.save(category));
    }
    
    // Brands
    public List<BrandDto> getAllActiveBrands() {
        return brandRepo.findByIsActiveTrueOrderByName()
                .stream()
                .map(this::toBrandDto)
                .collect(Collectors.toList());
    }
    
    // Admin method to get ALL brands (including inactive)
    public List<BrandDto> getAllBrands() {
        return brandRepo.findAllByOrderByName()
                .stream()
                .map(this::toBrandDto)
                .collect(Collectors.toList());
    }
    
    // Admin method to get ALL products (including inactive)
    public List<ProductDto> getAllProducts() {
        return productRepo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }
    
    // Admin method to get product by ID (including inactive)
    public ProductDto getProductById(String id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ApiException("PRODUCT_NOT_FOUND", "Product not found"));
        return toProductDtoWithDetails(product);
    }
    
    public BrandDto getBrandBySlug(String slug) {
        Brand brand = brandRepo.findBySlug(slug)
                .orElseThrow(() -> new ApiException("BRAND_NOT_FOUND", "Brand not found"));
        return toBrandDto(brand);
    }
    
    @Transactional
    public BrandDto createBrand(CreateBrandRequest request) {
        if (brandRepo.existsBySlug(request.getSlug())) {
            throw new ApiException("SLUG_EXISTS", "Brand slug already exists");
        }
        
        Brand brand = Brand.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .build();
                
        return toBrandDto(brandRepo.save(brand));
    }
    
    // Products
    public Page<ProductDto> searchProducts(String query, String categorySlug, String brandSlug, 
            String condition, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        
        // Convert slugs to IDs
        String categoryId = null;
        if (categorySlug != null) {
            categoryId = categoryRepo.findBySlug(categorySlug)
                    .map(Category::getId)
                    .orElse(null);
        }
        
        String brandId = null;
        if (brandSlug != null) {
            brandId = brandRepo.findBySlug(brandSlug)
                    .map(Brand::getId)
                    .orElse(null);
        }
        
        ConditionGrade conditionGrade = null;
        if (condition != null) {
            conditionGrade = ConditionGrade.valueOf(condition.toUpperCase());
        }
        
        Page<Product> products;
        if (query != null && !query.trim().isEmpty()) {
            products = productRepo.searchProducts(query.trim(), pageable);
        } else {
            products = productRepo.findWithFilters(categoryId, brandId, conditionGrade, minPrice, maxPrice, pageable);
        }
        
        return products.map(this::toProductDto);
    }
    
    public ProductDto getProductBySlug(String slug) {
        Product product = productRepo.findBySlug(slug)
                .orElseThrow(() -> new ApiException("PRODUCT_NOT_FOUND", "Product not found"));
        return toProductDtoWithDetails(product);
    }
    
    // Mappers
    private CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .parentId(category.getParentId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .isActive(category.isActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .build();
    }
    
    private CategoryDto toCategoryDtoWithChildren(Category category) {
        CategoryDto dto = toCategoryDto(category);
        List<Category> children = categoryRepo.findByParentIdAndIsActiveTrueOrderBySortOrder(category.getId());
        dto.setChildren(children.stream().map(this::toCategoryDto).collect(Collectors.toList()));
        return dto;
    }
    
    private BrandDto toBrandDto(Brand brand) {
        return BrandDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .isActive(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .build();
    }
    
    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .description(product.getDescription())
                .conditionGrade(product.getConditionGrade())
                .warrantyMonths(product.getWarrantyMonths())
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
    
    private ProductDto toProductDtoWithDetails(Product product) {
        ProductDto dto = toProductDto(product);
        
        // Add category and brand details
        categoryRepo.findById(product.getCategoryId()).ifPresent(cat -> dto.setCategory(toCategoryDto(cat)));
        brandRepo.findById(product.getBrandId()).ifPresent(brand -> dto.setBrand(toBrandDto(brand)));
        
        // Add variants
        List<ProductVariant> variants = variantRepo.findByProductIdAndIsActiveTrueOrderByCreatedAt(product.getId());
        dto.setVariants(variants.stream().map(this::toVariantDto).collect(Collectors.toList()));
        
        return dto;
    }
    
    private ProductVariantDto toVariantDto(ProductVariant variant) {
        ProductVariantDto dto = ProductVariantDto.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .sku(variant.getSku())
                .mpn(variant.getMpn())
                .color(variant.getColor())
                .storageGb(variant.getStorageGb())
                .ramGb(variant.getRamGb())
                .priceMrp(variant.getPriceMrp())
                .priceSale(variant.getPriceSale())
                .taxRate(variant.getTaxRate())
                .weightGrams(variant.getWeightGrams())
                .isActive(variant.isActive())
                .createdAt(variant.getCreatedAt())
                .build();
                
        // Add inventory
        inventoryRepo.findByVariantId(variant.getId()).ifPresent(inv -> {
            dto.setInventory(InventoryDto.builder()
                    .variantId(inv.getVariantId())
                    .quantity(inv.getQuantity())
                    .safetyStock(inv.getSafetyStock())
                    .reserved(inv.getReserved())
                    .available(inv.getAvailable())
                    .inStock(inv.isInStock())
                    .lowStock(inv.isLowStock())
                    .build());
        });
        
        return dto;
    }
}
