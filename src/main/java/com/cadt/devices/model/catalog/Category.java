package com.cadt.devices.model.catalog;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_category_slug", columnList = "slug", unique = true),
        @Index(name = "idx_category_parent", columnList = "parentId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category extends BaseEntity {

    private String parentId;

    @NotBlank
    @Column(length = 100, nullable = false)
    private String name;

    @NotBlank
    @Column(length = 120, nullable = false, unique = true)
    private String slug;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;

    // virtual fields for hierarchy
    @OneToMany(mappedBy = "parentId", fetch = FetchType.LAZY)
    private List<Category> children;
}
