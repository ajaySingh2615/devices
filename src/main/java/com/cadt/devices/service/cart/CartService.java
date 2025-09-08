package com.cadt.devices.service.cart;

import com.cadt.devices.dto.cart.*;
import com.cadt.devices.dto.catalog.ProductVariantDto;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.cart.Cart;
import com.cadt.devices.model.cart.CartItem;
import com.cadt.devices.model.catalog.ProductVariant;
import com.cadt.devices.repo.cart.CartItemRepository;
import com.cadt.devices.repo.cart.CartRepository;
import com.cadt.devices.repo.catalog.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository cartItemRepo;
    private final ProductVariantRepository variantRepo;

    @Transactional
    public CartDto getOrCreateCart(String userId, String sessionId) {
        log.debug("Getting or creating cart for userId: {}, sessionId: {}", userId, sessionId);
        
        Cart cart = cartRepo.findByUserIdOrSessionId(userId, sessionId)
                .orElseGet(() -> createCart(userId, sessionId));
        
        return toCartDto(cart);
    }

    @Transactional
    public CartDto addToCart(String userId, String sessionId, AddToCartRequest request) {
        log.debug("Adding to cart: userId={}, sessionId={}, variantId={}, quantity={}", 
                userId, sessionId, request.getVariantId(), request.getQuantity());

        // Get or create cart
        Cart cart = cartRepo.findByUserIdOrSessionId(userId, sessionId)
                .orElseGet(() -> createCart(userId, sessionId));

        // Validate variant exists and is active
        ProductVariant variant = variantRepo.findById(request.getVariantId())
                .orElseThrow(() -> new ApiException("VARIANT_NOT_FOUND", "Product variant not found"));
        
        if (!variant.isActive()) {
            throw new ApiException("VARIANT_INACTIVE", "Product variant is not available");
        }

        // Check if item already exists in cart
        CartItem existingItem = cartItemRepo.findByCartIdAndVariantId(cart.getId(), request.getVariantId())
                .orElse(null);

        if (existingItem != null) {
            // Update quantity
            existingItem.setQuantity(existingItem.getQuantity() + request.getQuantity());
            cartItemRepo.save(existingItem);
        } else {
            // Create new cart item
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .variantId(request.getVariantId())
                    .quantity(request.getQuantity())
                    .priceSnapshot(variant.getPriceSale())
                    .taxRateSnapshot(variant.getTaxRate())
                    .build();
            cartItemRepo.save(newItem);
        }

        return toCartDto(cart);
    }

    @Transactional
    public CartDto updateCartItem(String userId, String sessionId, String itemId, UpdateCartItemRequest request) {
        log.debug("Updating cart item: userId={}, sessionId={}, itemId={}, quantity={}", 
                userId, sessionId, itemId, request.getQuantity());

        Cart cart = getCartForUser(userId, sessionId);
        
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new ApiException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ApiException("CART_ITEM_NOT_FOUND", "Cart item not found");
        }

        item.setQuantity(request.getQuantity());
        cartItemRepo.save(item);

        return toCartDto(cart);
    }

    @Transactional
    public CartDto removeFromCart(String userId, String sessionId, String itemId) {
        log.debug("Removing from cart: userId={}, sessionId={}, itemId={}", userId, sessionId, itemId);

        Cart cart = getCartForUser(userId, sessionId);
        
        CartItem item = cartItemRepo.findById(itemId)
                .orElseThrow(() -> new ApiException("CART_ITEM_NOT_FOUND", "Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new ApiException("CART_ITEM_NOT_FOUND", "Cart item not found");
        }

        cartItemRepo.delete(item);

        return toCartDto(cart);
    }

    @Transactional
    public CartDto clearCart(String userId, String sessionId) {
        log.debug("Clearing cart: userId={}, sessionId={}", userId, sessionId);

        Cart cart = getCartForUser(userId, sessionId);
        cartItemRepo.deleteByCartId(cart.getId());

        return toCartDto(cart);
    }

    @Transactional
    public CartDto mergeCarts(String userId, String sessionId) {
        log.debug("Merging carts: userId={}, sessionId={}", userId, sessionId);

        Cart userCart = cartRepo.findByUserId(userId).orElse(null);
        Cart sessionCart = cartRepo.findBySessionId(sessionId).orElse(null);

        if (sessionCart == null) {
            return userCart != null ? toCartDto(userCart) : getOrCreateCart(userId, null);
        }

        if (userCart == null) {
            // Convert session cart to user cart
            sessionCart.setUserId(userId);
            sessionCart.setSessionId(null);
            return toCartDto(cartRepo.save(sessionCart));
        }

        // Merge session cart items into user cart
        List<CartItem> sessionItems = cartItemRepo.findByCartIdOrderByCreatedAtAsc(sessionCart.getId());
        for (CartItem sessionItem : sessionItems) {
            CartItem existingItem = cartItemRepo.findByCartIdAndVariantId(userCart.getId(), sessionItem.getVariantId())
                    .orElse(null);

            if (existingItem != null) {
                existingItem.setQuantity(existingItem.getQuantity() + sessionItem.getQuantity());
                cartItemRepo.save(existingItem);
            } else {
                sessionItem.setCart(userCart);
                cartItemRepo.save(sessionItem);
            }
        }

        // Delete session cart
        cartRepo.delete(sessionCart);

        return toCartDto(userCart);
    }

    private Cart createCart(String userId, String sessionId) {
        Cart cart = Cart.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();
        return cartRepo.save(cart);
    }

    private Cart getCartForUser(String userId, String sessionId) {
        return cartRepo.findByUserIdOrSessionId(userId, sessionId)
                .orElseThrow(() -> new ApiException("CART_NOT_FOUND", "Cart not found"));
    }

    private CartDto toCartDto(Cart cart) {
        List<CartItem> items = cartItemRepo.findByCartIdOrderByCreatedAtAsc(cart.getId());
        
        BigDecimal subtotal = items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxTotal = items.stream()
                .map(CartItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .items(items.stream().map(this::toCartItemDto).collect(Collectors.toList()))
                .totalItems(cart.getTotalItems())
                .subtotal(subtotal)
                .taxTotal(taxTotal)
                .grandTotal(subtotal.add(taxTotal))
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDto toCartItemDto(CartItem item) {
        ProductVariant variant = variantRepo.findById(item.getVariantId()).orElse(null);
        ProductVariantDto variantDto = variant != null ? toVariantDto(variant) : null;

        return CartItemDto.builder()
                .id(item.getId())
                .cartId(item.getCart().getId())
                .variantId(item.getVariantId())
                .quantity(item.getQuantity())
                .priceSnapshot(item.getPriceSnapshot())
                .taxRateSnapshot(item.getTaxRateSnapshot())
                .subtotal(item.getSubtotal())
                .taxAmount(item.getTaxAmount())
                .total(item.getTotal())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
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
