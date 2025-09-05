package com.cadt.devices.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {
    private String parentId;
    
    @NotBlank(message = "Category name is required")
    private String name;
    
    @NotBlank(message = "Category slug is required")
    private String slug;
    
    private String description;
    private Integer sortOrder = 0;
}
