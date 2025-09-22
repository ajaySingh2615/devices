package com.cadt.devices.dto.catalog;

import com.cadt.devices.model.catalog.ConditionGrade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class CreateProductRequest {
    @NotBlank(message = "Category ID is required")
    private String categoryId;
    
    @NotBlank(message = "Brand ID is required")
    private String brandId;
    
    @NotBlank(message = "Product title is required")
    private String title;
    
    @NotBlank(message = "Product slug is required")
    private String slug;
    
    private String description;
    
    @NotNull(message = "Condition grade is required")
    private ConditionGrade conditionGrade;
    
    private Integer warrantyMonths = 6;

    @JsonProperty("isBestseller")
    private Boolean isBestseller;
}
