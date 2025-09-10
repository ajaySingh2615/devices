package com.cadt.devices.model.cart;

import com.cadt.devices.model.common.BaseEntity;
import com.cadt.devices.model.coupon.Coupon;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_user", columnList = "userId"),
        @Index(name = "idx_cart_session", columnList = "sessionId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart extends BaseEntity {

    @Column(name = "user_id")
    private String userId; // null for anonymous carts

    @Column(name = "session_id", length = 100)
    private String sessionId; // for anonymous carts

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    // Coupon information
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_coupon_id")
    private Coupon appliedCoupon;

    @Column(name = "coupon_discount", precision = 10, scale = 2)
    private BigDecimal couponDiscount;

    @Column(name = "final_total", precision = 10, scale = 2)
    private BigDecimal finalTotal;

    // Helper methods
    public void addItem(CartItem item) {
        items.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        item.setCart(null);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
