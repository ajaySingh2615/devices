package com.cadt.devices.dto.catalog;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BrandDto {

    private String id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private boolean isActive;
    private Instant createdAt;
}
