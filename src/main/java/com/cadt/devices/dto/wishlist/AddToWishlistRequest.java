package com.cadt.devices.dto.wishlist;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToWishlistRequest {
    
    @NotBlank(message = "Variant ID is required")
    private String variantId;
}
