package com.cadt.devices.model.wishlist;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "wishlist_items", indexes = {
        @Index(name = "idx_wishlist_item_wishlist", columnList = "wishlistId"),
        @Index(name = "idx_wishlist_item_variant", columnList = "variantId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wishlist_id", nullable = false)
    private Wishlist wishlist;

    @Column(name = "variant_id", nullable = false)
    private String variantId;
}
