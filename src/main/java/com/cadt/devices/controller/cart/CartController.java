package com.cadt.devices.controller.cart;

import com.cadt.devices.dto.cart.*;
import com.cadt.devices.dto.coupon.*;
import com.cadt.devices.service.cart.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartDto> getCart(
            Authentication authentication,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(request);
        
        log.debug("Getting cart for userId: {}, sessionId: {}", userId, sessionIdToUse);
        
        CartDto cart = cartService.getOrCreateCart(userId, sessionIdToUse);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/items")
    public ResponseEntity<CartDto> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest httpRequest) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(httpRequest);
        
        log.debug("Adding to cart: userId={}, sessionId={}, request={}", userId, sessionIdToUse, request);
        
        CartDto cart = cartService.addToCart(userId, sessionIdToUse, request);
        return ResponseEntity.ok(cart);
    }

    @PatchMapping("/items/{itemId}")
    public ResponseEntity<CartDto> updateCartItem(
            Authentication authentication,
            @PathVariable String itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest httpRequest) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(httpRequest);
        
        log.debug("Updating cart item: userId={}, sessionId={}, itemId={}, request={}", 
                userId, sessionIdToUse, itemId, request);
        
        CartDto cart = cartService.updateCartItem(userId, sessionIdToUse, itemId, request);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartDto> removeFromCart(
            Authentication authentication,
            @PathVariable String itemId,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(request);
        
        log.debug("Removing from cart: userId={}, sessionId={}, itemId={}", userId, sessionIdToUse, itemId);
        
        CartDto cart = cartService.removeFromCart(userId, sessionIdToUse, itemId);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping
    public ResponseEntity<CartDto> clearCart(
            Authentication authentication,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(request);
        
        log.debug("Clearing cart: userId={}, sessionId={}", userId, sessionIdToUse);
        
        CartDto cart = cartService.clearCart(userId, sessionIdToUse);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/merge")
    public ResponseEntity<CartDto> mergeCarts(
            Authentication authentication,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(request);
        
        log.debug("Merging carts: userId={}, sessionId={}", userId, sessionIdToUse);
        
        CartDto cart = cartService.mergeCarts(userId, sessionIdToUse);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/apply-coupon")
    public ResponseEntity<CouponApplicationResult> applyCoupon(
            Authentication authentication,
            @RequestBody ApplyCouponRequest request,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest httpRequest) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(httpRequest);
        
        log.info("=== COUPON APPLICATION REQUEST ===");
        log.info("Coupon Code: {}", request.getCode());
        log.info("User ID: {}", userId);
        log.info("Session ID: {}", sessionIdToUse);
        log.info("Authentication: {}", authentication != null ? "Authenticated" : "Anonymous");
        
        try {
            CouponApplicationResult result = cartService.applyCoupon(userId, sessionIdToUse, request.getCode());
            log.info("Coupon application result: success={}, message={}", result.isSuccess(), result.getMessage());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error applying coupon: {}", e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/apply-coupon")
    public ResponseEntity<Void> removeCoupon(
            Authentication authentication,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(request);
        
        log.debug("Removing coupon from cart for userId: {}", userId);
        
        cartService.removeCoupon(userId, sessionIdToUse);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/with-coupon")
    public ResponseEntity<CartDto> getCartWithCoupon(
            Authentication authentication,
            @RequestParam(required = false) String couponCode,
            @RequestParam(required = false) String sessionId,
            HttpServletRequest request) {
        
        String userId = authentication != null ? authentication.getName() : null;
        String sessionIdToUse = sessionId != null ? sessionId : getSessionId(request);
        
        log.debug("Getting cart with coupon validation for userId: {}, coupon: {}", userId, couponCode);
        
        CartDto cart = cartService.getCartWithCoupon(userId, sessionIdToUse, couponCode);
        return ResponseEntity.ok(cart);
    }


    private String getSessionId(HttpServletRequest request) {
        // Generate a simple session ID based on request attributes
        // In production, you might want to use a more sophisticated approach
        String sessionId = (String) request.getAttribute("sessionId");
        if (sessionId == null) {
            sessionId = request.getSession().getId();
            request.setAttribute("sessionId", sessionId);
        }
        return sessionId;
    }
}
