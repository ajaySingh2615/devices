package com.cadt.devices.service.wishlist;

import com.cadt.devices.dto.wishlist.*;
import com.cadt.devices.dto.catalog.ProductVariantDto;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.wishlist.Wishlist;
import com.cadt.devices.model.wishlist.WishlistItem;
import com.cadt.devices.model.catalog.ProductVariant;
import com.cadt.devices.repo.wishlist.WishlistItemRepository;
import com.cadt.devices.repo.wishlist.WishlistRepository;
import com.cadt.devices.repo.catalog.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistService {

    private final WishlistRepository wishlistRepo;
    private final WishlistItemRepository wishlistItemRepo;
    private final ProductVariantRepository variantRepo;

    @Transactional
    public WishlistDto getOrCreateWishlist(String userId) {
        log.debug("Getting or creating wishlist for userId: {}", userId);
        
        Wishlist wishlist = wishlistRepo.findByUserId(userId)
                .orElseGet(() -> createWishlist(userId));
        
        return toWishlistDto(wishlist);
    }

    @Transactional
    public WishlistDto addToWishlist(String userId, AddToWishlistRequest request) {
        log.debug("Adding to wishlist: userId={}, variantId={}", userId, request.getVariantId());

        // Get or create wishlist
        Wishlist wishlist = wishlistRepo.findByUserId(userId)
                .orElseGet(() -> createWishlist(userId));

        // Validate variant exists and is active
        ProductVariant variant = variantRepo.findById(request.getVariantId())
                .orElseThrow(() -> new ApiException("VARIANT_NOT_FOUND", "Product variant not found"));
        
        if (!variant.isActive()) {
            throw new ApiException("VARIANT_INACTIVE", "Product variant is not available");
        }

        // Check if item already exists in wishlist
        if (wishlistItemRepo.existsByWishlistIdAndVariantId(wishlist.getId(), request.getVariantId())) {
            throw new ApiException("ITEM_ALREADY_IN_WISHLIST", "Item is already in wishlist");
        }

        // Create new wishlist item
        WishlistItem newItem = WishlistItem.builder()
                .wishlist(wishlist)
                .variantId(request.getVariantId())
                .build();
        wishlistItemRepo.save(newItem);

        return toWishlistDto(wishlist);
    }

    @Transactional
    public WishlistDto removeFromWishlist(String userId, String itemId) {
        log.debug("Removing from wishlist: userId={}, itemId={}", userId, itemId);

        Wishlist wishlist = getWishlistForUser(userId);
        
        WishlistItem item = wishlistItemRepo.findById(itemId)
                .orElseThrow(() -> new ApiException("WISHLIST_ITEM_NOT_FOUND", "Wishlist item not found"));

        if (!item.getWishlist().getId().equals(wishlist.getId())) {
            throw new ApiException("WISHLIST_ITEM_NOT_FOUND", "Wishlist item not found");
        }

        wishlistItemRepo.delete(item);

        return toWishlistDto(wishlist);
    }

    @Transactional
    public WishlistDto removeFromWishlistByVariant(String userId, String variantId) {
        log.debug("Removing from wishlist by variant: userId={}, variantId={}", userId, variantId);

        Wishlist wishlist = getWishlistForUser(userId);
        wishlistItemRepo.deleteByWishlistIdAndVariantId(wishlist.getId(), variantId);

        return toWishlistDto(wishlist);
    }

    @Transactional
    public WishlistDto clearWishlist(String userId) {
        log.debug("Clearing wishlist: userId={}", userId);

        Wishlist wishlist = getWishlistForUser(userId);
        wishlistItemRepo.deleteByWishlistId(wishlist.getId());

        return toWishlistDto(wishlist);
    }

    public boolean isInWishlist(String userId, String variantId) {
        log.debug("Checking if variant is in wishlist: userId={}, variantId={}", userId, variantId);

        Wishlist wishlist = wishlistRepo.findByUserId(userId).orElse(null);
        if (wishlist == null) {
            return false;
        }

        return wishlistItemRepo.existsByWishlistIdAndVariantId(wishlist.getId(), variantId);
    }

    private Wishlist createWishlist(String userId) {
        Wishlist wishlist = Wishlist.builder()
                .userId(userId)
                .build();
        return wishlistRepo.save(wishlist);
    }

    private Wishlist getWishlistForUser(String userId) {
        return wishlistRepo.findByUserId(userId)
                .orElseThrow(() -> new ApiException("WISHLIST_NOT_FOUND", "Wishlist not found"));
    }

    private WishlistDto toWishlistDto(Wishlist wishlist) {
        List<WishlistItem> items = wishlistItemRepo.findByWishlistIdOrderByCreatedAtAsc(wishlist.getId());

        return WishlistDto.builder()
                .id(wishlist.getId())
                .userId(wishlist.getUserId())
                .items(items.stream().map(this::toWishlistItemDto).collect(Collectors.toList()))
                .totalItems(wishlist.getTotalItems())
                .createdAt(wishlist.getCreatedAt())
                .updatedAt(wishlist.getUpdatedAt())
                .build();
    }

    private WishlistItemDto toWishlistItemDto(WishlistItem item) {
        ProductVariant variant = variantRepo.findById(item.getVariantId()).orElse(null);
        ProductVariantDto variantDto = variant != null ? toVariantDto(variant) : null;

        return WishlistItemDto.builder()
                .id(item.getId())
                .wishlistId(item.getWishlist().getId())
                .variantId(item.getVariantId())
                .createdAt(item.getCreatedAt())
                .variant(variantDto)
                .build();
    }

    private ProductVariantDto toVariantDto(ProductVariant variant) {
        return ProductVariantDto.builder()
                .id(variant.getId())
                .productId(variant.getProductId())
                .sku(variant.getSku())
                .mpn(variant.getMpn())
                .color(variant.getColor())
                .storageGb(variant.getStorageGb())
                .ramGb(variant.getRamGb())
                .priceMrp(variant.getPriceMrp())
                .priceSale(variant.getPriceSale())
                .taxRate(variant.getTaxRate())
                .weightGrams(variant.getWeightGrams())
                .isActive(variant.isActive())
                .createdAt(variant.getCreatedAt())
                .build();
    }
}
