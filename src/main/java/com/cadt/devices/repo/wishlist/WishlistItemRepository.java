package com.cadt.devices.repo.wishlist;

import com.cadt.devices.model.wishlist.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {

    // Find wishlist items by wishlist ID
    List<WishlistItem> findByWishlistIdOrderByCreatedAtAsc(String wishlistId);

    // Find wishlist item by wishlist ID and variant ID
    Optional<WishlistItem> findByWishlistIdAndVariantId(String wishlistId, String variantId);

    // Check if variant exists in wishlist
    boolean existsByWishlistIdAndVariantId(String wishlistId, String variantId);

    // Delete wishlist items by wishlist ID
    void deleteByWishlistId(String wishlistId);

    // Delete wishlist item by wishlist ID and variant ID
    void deleteByWishlistIdAndVariantId(String wishlistId, String variantId);

    // Count items in wishlist
    @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.wishlist.id = :wishlistId")
    int countByWishlistId(@Param("wishlistId") String wishlistId);
}
