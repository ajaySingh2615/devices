package com.cadt.devices.repo.cart;

import com.cadt.devices.model.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, String> {

    // Find cart items by cart ID
    List<CartItem> findByCartIdOrderByCreatedAtAsc(String cartId);

    // Find cart item by cart ID and variant ID
    Optional<CartItem> findByCartIdAndVariantId(String cartId, String variantId);

    // Check if variant exists in cart
    boolean existsByCartIdAndVariantId(String cartId, String variantId);

    // Delete cart items by cart ID
    void deleteByCartId(String cartId);

    // Delete cart item by cart ID and variant ID
    void deleteByCartIdAndVariantId(String cartId, String variantId);

    // Count items in cart
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int countByCartId(@Param("cartId") String cartId);

    // Get total quantity in cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId")
    int getTotalQuantityByCartId(@Param("cartId") String cartId);
}
