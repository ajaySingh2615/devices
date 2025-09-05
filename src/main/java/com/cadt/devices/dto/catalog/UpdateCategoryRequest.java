package com.cadt.devices.dto.catalog;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String parentId;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;
    private Integer sortOrder;
}
