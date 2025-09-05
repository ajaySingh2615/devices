package com.cadt.devices.model.catalog;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "inventory", indexes = {
        @Index(name = "idx_inventory_variant", columnList = "variantId", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String variantId;

    @Column(nullable = false)
    @Builder.Default
    private int quantity = 0;

    @Column(nullable = false)
    @Builder.Default
    private int safetyStock = 5;

    @Column(nullable = false)
    @Builder.Default
    private int reserved = 0; // Reserved for pending orders

    public int getAvailable() {
        return Math.max(0, quantity - reserved);
    }

    public boolean isInStock() {
        return getAvailable() > 0;
    }

    public boolean isLowStock() {
        return getAvailable() <= safetyStock;
    }
}
