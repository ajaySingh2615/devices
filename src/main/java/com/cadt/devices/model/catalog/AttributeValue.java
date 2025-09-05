package com.cadt.devices.model.catalog;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "attribute_values", indexes = {
        @Index(name = "idx_attr_value_attr", columnList = "attributeId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttributeValue extends BaseEntity {

    @Column(nullable = false)
    private String attributeId;

    @NotBlank
    @Column(length = 200, nullable = false)
    private String value;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
