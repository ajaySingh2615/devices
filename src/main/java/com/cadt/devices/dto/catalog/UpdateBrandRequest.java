package com.cadt.devices.dto.catalog;

import lombok.Data;

@Data
public class UpdateBrandRequest {
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private Boolean isActive;
}
