package com.cadt.devices.model.catalog;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_product_slug", columnList = "slug", unique = true),
        @Index(name = "idx_product_category", columnList = "categoryId"),
        @Index(name = "idx_product_brand", columnList = "brandId"),
        @Index(name = "idx_product_active", columnList = "isActive")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String categoryId;

    @Column(nullable = false)
    private String brandId;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String title;

    @NotBlank
    @Column(length = 250, nullable = false, unique = true)
    private String slug;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConditionGrade conditionGrade;

    @Column(nullable = false)
    @Builder.Default
    private int warrantyMonths = 6;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive;

    // Virtual relationship to variants
    @OneToMany(mappedBy = "productId", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<ProductVariant> variants;
}
