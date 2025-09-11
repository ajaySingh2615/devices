package com.cadt.devices.service.cart;

import com.cadt.devices.dto.cart.*;
import com.cadt.devices.dto.catalog.*;
import com.cadt.devices.dto.coupon.*;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.cart.Cart;
import com.cadt.devices.model.cart.CartItem;
import com.cadt.devices.model.catalog.*;
import com.cadt.devices.model.media.Media;
import com.cadt.devices.model.media.MediaOwnerType;
import com.cadt.devices.repo.cart.CartItemRepository;
import com.cadt.devices.repo.cart.CartRepository;
import com.cadt.devices.repo.catalog.*;
import com.cadt.devices.repo.media.MediaRepository;
import com.cadt.devices.service.coupon.CouponService;
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
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final BrandRepository brandRepo;
    private final MediaRepository mediaRepo;
    private final CouponService couponService;

    @Transactional
    public CartDto getOrCreateCart(String userId, String sessionId) {
        log.debug("Getting or creating cart for userId: {}, sessionId: {}", userId, sessionId);
        
        Cart cart = cartRepo.findByUserIdOrSessionId(userId, sessionId)
                .orElseGet(() -> createCart(userId, sessionId));
        
        return toCartDto(cart);
    }

    @Transactional
    public Cart getOrCreateCartEntity(String userId, String sessionId) {
        log.debug("Getting or creating cart entity for userId: {}, sessionId: {}", userId, sessionId);
        
        return cartRepo.findByUserIdOrSessionId(userId, sessionId)
                .orElseGet(() -> createCart(userId, sessionId));
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
    public void clearAllCartsFor(String userId, String sessionId) {
        log.debug("Clearing all carts for userId={}, sessionId={}", userId, sessionId);
        // Clear by user cart if exists
        cartRepo.findByUserId(userId).ifPresent(c -> cartItemRepo.deleteByCartId(c.getId()));
        // Clear by session cart if exists
        if (sessionId != null) {
            cartRepo.findBySessionId(sessionId).ifPresent(c -> cartItemRepo.deleteByCartId(c.getId()));
        }
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

        BigDecimal grandTotal = subtotal.add(taxTotal);
        
        // Include coupon information from cart entity
        CouponDto appliedCouponDto = null;
        if (cart.getAppliedCoupon() != null) {
            appliedCouponDto = couponService.toCouponDto(cart.getAppliedCoupon());
        }

        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .items(items.stream().map(this::toCartItemDto).collect(Collectors.toList()))
                .totalItems(cart.getTotalItems())
                .subtotal(subtotal)
                .taxTotal(taxTotal)
                .grandTotal(grandTotal)
                .appliedCoupon(appliedCouponDto)
                .couponDiscount(cart.getCouponDiscount())
                .finalTotal(cart.getFinalTotal() != null ? cart.getFinalTotal() : grandTotal)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private CartItemDto toCartItemDto(CartItem item) {
        ProductVariant variant = variantRepo.findById(item.getVariantId()).orElse(null);
        ProductVariantDto variantDto = variant != null ? toVariantDto(variant) : null;
        
        // Fetch product information if variant exists
        ProductDto productDto = null;
        if (variant != null) {
            Product product = productRepo.findById(variant.getProductId()).orElse(null);
            productDto = product != null ? toProductDtoWithDetails(product) : null;
        }

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
                .product(productDto)
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

    private ProductDto toProductDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .title(product.getTitle())
                .slug(product.getSlug())
                .description(product.getDescription())
                .conditionGrade(product.getConditionGrade())
                .warrantyMonths(product.getWarrantyMonths())
                .isActive(product.isActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
    
    private ProductDto toProductDtoWithDetails(Product product) {
        ProductDto dto = toProductDto(product);
        
        // Add category and brand details
        categoryRepo.findById(product.getCategoryId()).ifPresent(cat -> dto.setCategory(toCategoryDto(cat)));
        brandRepo.findById(product.getBrandId()).ifPresent(brand -> dto.setBrand(toBrandDto(brand)));
        
        // Add variants
        List<ProductVariant> variants = variantRepo.findByProductIdAndIsActiveTrueOrderByCreatedAt(product.getId());
        dto.setVariants(variants.stream().map(this::toVariantDto).collect(Collectors.toList()));
        
        // Add media/images
        List<Media> media = mediaRepo.findByOwnerTypeAndOwnerIdOrderBySortOrder(MediaOwnerType.PRODUCT, product.getId());
        dto.setImages(media.stream().map(this::toMediaDto).collect(Collectors.toList()));
        
        return dto;
    }
    
    private CategoryDto toCategoryDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .parentId(category.getParentId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .isActive(category.isActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt())
                .build();
    }
    
    private BrandDto toBrandDto(Brand brand) {
        return BrandDto.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .isActive(brand.isActive())
                .createdAt(brand.getCreatedAt())
                .build();
    }
    
    private MediaDto toMediaDto(Media media) {
        return MediaDto.builder()
                .id(media.getId())
                .url(media.getUrl())
                .type(media.getType())
                .alt(media.getAlt())
                .sortOrder(media.getSortOrder())
                .build();
    }

    @Transactional
    public CouponApplicationResult applyCoupon(String userId, String sessionId, String couponCode) {
        log.debug("Applying coupon: {} to cart for user: {}", couponCode, userId);
        
        Cart cart = getOrCreateCartEntity(userId, sessionId);
        BigDecimal subtotal = calculateSubtotal(cart);
        
        CouponApplicationResult result = couponService.applyCoupon(couponCode, subtotal, userId);
        
        if (result.isSuccess()) {
            // Store applied coupon in cart
            cart.setAppliedCoupon(couponService.getCouponEntityByCode(couponCode));
            cart.setCouponDiscount(result.getDiscountAmount());
            cart.setFinalTotal(result.getFinalAmount());
            
            cartRepo.save(cart);
            log.info("Coupon applied successfully: {} - discount: ₹{}", couponCode, result.getDiscountAmount());
        }
        
        return result;
    }

    @Transactional
    public void removeCoupon(String userId, String sessionId) {
        log.debug("Removing coupon from cart for user: {}", userId);
        
        Cart cart = getOrCreateCartEntity(userId, sessionId);
        
        // Clear applied coupon from cart
        cart.setAppliedCoupon(null);
        cart.setCouponDiscount(null);
        cart.setFinalTotal(null);
        
        cartRepo.save(cart);
        log.info("Coupon removed from cart for user: {}", userId);
    }

    @Transactional(readOnly = true)
    public CartDto getCartWithCoupon(String userId, String sessionId, String couponCode) {
        log.debug("Getting cart with coupon validation for user: {}", userId);
        
        Cart cart = getOrCreateCartEntity(userId, sessionId);
        BigDecimal subtotal = calculateSubtotal(cart);
        
        // Validate coupon if provided
        CouponApplicationResult couponResult = null;
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            couponResult = couponService.validateCoupon(couponCode, subtotal, userId);
        }
        
        return toCartDtoWithCoupon(cart, couponResult);
    }

    private CartDto toCartDtoWithCoupon(Cart cart, CouponApplicationResult couponResult) {
        List<CartItemDto> itemDtos = cart.getItems().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());

        BigDecimal subtotal = calculateSubtotal(cart);
        BigDecimal taxTotal = calculateTaxTotal(cart);
        BigDecimal grandTotal = subtotal.add(taxTotal);
        
        BigDecimal couponDiscount = BigDecimal.ZERO;
        BigDecimal finalTotal = grandTotal;
        
        if (couponResult != null && couponResult.isSuccess()) {
            couponDiscount = couponResult.getDiscountAmount();
            finalTotal = grandTotal.subtract(couponDiscount);
        }

        return CartDto.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .sessionId(cart.getSessionId())
                .items(itemDtos)
                .totalItems(cart.getItems().size())
                .subtotal(subtotal)
                .taxTotal(taxTotal)
                .grandTotal(grandTotal)
                .appliedCoupon(couponResult != null && couponResult.isSuccess() ? couponResult.getCoupon() : null)
                .couponDiscount(couponDiscount)
                .finalTotal(finalTotal)
                .createdAt(cart.getCreatedAt())
                .updatedAt(cart.getUpdatedAt())
                .build();
    }

    private BigDecimal calculateSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateTaxTotal(Cart cart) {
        return cart.getItems().stream()
                .map(CartItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
