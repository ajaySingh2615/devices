package com.cadt.devices.controller.wishlist;

import com.cadt.devices.dto.wishlist.*;
import com.cadt.devices.service.wishlist.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<WishlistDto> getWishlist(Authentication authentication) {
        String userId = authentication.getName();
        log.debug("Getting wishlist for userId: {}", userId);
        
        WishlistDto wishlist = wishlistService.getOrCreateWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistDto> addToWishlist(
            Authentication authentication,
            @Valid @RequestBody AddToWishlistRequest request) {
        
        String userId = authentication.getName();
        log.debug("Adding to wishlist: userId={}, request={}", userId, request);
        
        WishlistDto wishlist = wishlistService.addToWishlist(userId, request);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<WishlistDto> removeFromWishlist(
            Authentication authentication,
            @PathVariable String itemId) {
        
        String userId = authentication.getName();
        log.debug("Removing from wishlist: userId={}, itemId={}", userId, itemId);
        
        WishlistDto wishlist = wishlistService.removeFromWishlist(userId, itemId);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping("/items/variant/{variantId}")
    public ResponseEntity<WishlistDto> removeFromWishlistByVariant(
            Authentication authentication,
            @PathVariable String variantId) {
        
        String userId = authentication.getName();
        log.debug("Removing from wishlist by variant: userId={}, variantId={}", userId, variantId);
        
        WishlistDto wishlist = wishlistService.removeFromWishlistByVariant(userId, variantId);
        return ResponseEntity.ok(wishlist);
    }

    @DeleteMapping
    public ResponseEntity<WishlistDto> clearWishlist(Authentication authentication) {
        String userId = authentication.getName();
        log.debug("Clearing wishlist: userId={}", userId);
        
        WishlistDto wishlist = wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(wishlist);
    }

    @GetMapping("/check/{variantId}")
    public ResponseEntity<Boolean> isInWishlist(
            Authentication authentication,
            @PathVariable String variantId) {
        
        String userId = authentication.getName();
        log.debug("Checking if variant is in wishlist: userId={}, variantId={}", userId, variantId);
        
        boolean isInWishlist = wishlistService.isInWishlist(userId, variantId);
        return ResponseEntity.ok(isInWishlist);
    }
}
