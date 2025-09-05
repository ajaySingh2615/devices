package com.cadt.devices.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBrandRequest {

    @NotBlank(message = "Brand name is required")
    private String name;

    @NotBlank(message = "Brand slug is required")
    private String slug;

    private String description;
    private String logoUrl;
}
