package com.cadt.devices.dto.catalog;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class CategoryDto {

    private String id;
    private String parentId;
    private String name;
    private String slug;
    private String description;
    private boolean isActive;
    private int sortOrder;
    private Instant createdAt;
    private List<CategoryDto> children;
}
