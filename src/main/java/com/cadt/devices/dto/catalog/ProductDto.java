package com.cadt.devices.dto.catalog;

import com.cadt.devices.model.catalog.ConditionGrade;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProductDto {
    private String id;
    private String categoryId;
    private String brandId;
    private String title;
    private String slug;
    private String description;
    private ConditionGrade conditionGrade;
    private int warrantyMonths;
    private boolean isActive;
    private Instant createdAt;

    // Related Data
    private CategoryDto category;
    private BrandDto brand;
    private List<ProductVariantDto> variants;
    private List<MediaDto> images;
}
