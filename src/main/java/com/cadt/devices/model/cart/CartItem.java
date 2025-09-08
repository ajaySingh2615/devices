package com.cadt.devices.model.cart;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cart_items", indexes = {
        @Index(name = "idx_cart_item_cart", columnList = "cartId"),
        @Index(name = "idx_cart_item_variant", columnList = "variantId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "variant_id", nullable = false)
    private String variantId;

    @Column(nullable = false)
    private Integer quantity;

    // Price snapshots at time of adding to cart
    @Column(name = "price_snapshot", precision = 10, scale = 2, nullable = false)
    private BigDecimal priceSnapshot;

    @Column(name = "tax_rate_snapshot", precision = 5, scale = 2, nullable = false)
    private BigDecimal taxRateSnapshot;

    // Helper methods
    public BigDecimal getSubtotal() {
        return priceSnapshot.multiply(BigDecimal.valueOf(quantity));
    }

    public BigDecimal getTaxAmount() {
        return getSubtotal().multiply(taxRateSnapshot.divide(BigDecimal.valueOf(100)));
    }

    public BigDecimal getTotal() {
        return getSubtotal().add(getTaxAmount());
    }
}
