package com.cadt.devices.dto.catalog;

import com.cadt.devices.model.catalog.ConditionGrade;
import lombok.Data;

@Data
public class UpdateProductRequest {
    private String categoryId;
    private String brandId;
    private String title;
    private String slug;
    private String description;
    private ConditionGrade conditionGrade;
    private Integer warrantyMonths;
    private Boolean isActive;
}
