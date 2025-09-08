package com.cadt.devices.dto.wishlist;

import com.cadt.devices.dto.catalog.ProductVariantDto;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class WishlistItemDto {
    private String id;
    private String wishlistId;
    private String variantId;
    private Instant createdAt;
    
    // Related data
    private ProductVariantDto variant;
}
