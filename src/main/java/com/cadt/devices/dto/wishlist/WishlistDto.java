package com.cadt.devices.dto.wishlist;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class WishlistDto {
    private String id;
    private String userId;
    private List<WishlistItemDto> items;
    private int totalItems;
    private Instant createdAt;
    private Instant updatedAt;
}
