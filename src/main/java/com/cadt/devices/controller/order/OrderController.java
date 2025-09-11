package com.cadt.devices.controller.order;

import com.cadt.devices.dto.order.OrderDto;
import com.cadt.devices.dto.order.PlaceOrderRequest;
import com.cadt.devices.dto.order.PlaceOrderResponse;
import com.cadt.devices.service.order.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PlaceOrderResponse> placeOrder(
            @Valid @RequestBody PlaceOrderRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        log.info("Placing order for user: {}", userId);
        
        PlaceOrderResponse response = orderService.placeOrder(userId, null, request);
        
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderDto>> getUserOrders(Authentication authentication) {
        String userId = authentication.getName();
        log.debug("Getting orders for user: {}", userId);
        
        List<OrderDto> orders = orderService.getUserOrders(userId);
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/paginated")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<OrderDto>> getUserOrdersPaginated(
            Authentication authentication,
            Pageable pageable) {
        String userId = authentication.getName();
        log.debug("Getting orders for user: {} with pagination", userId);
        
        Page<OrderDto> orders = orderService.getUserOrders(userId, pageable);
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable String orderId,
            Authentication authentication) {
        String userId = authentication.getName();
        log.debug("Getting order: {} for user: {}", orderId, userId);
        
        OrderDto order = orderService.getOrderById(orderId, userId);
        
        return ResponseEntity.ok(order);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        log.debug("Getting all orders (admin)");
        
        List<OrderDto> orders = orderService.getAllOrders();
        
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/paginated")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderDto>> getAllOrdersPaginated(Pageable pageable) {
        log.debug("Getting all orders with pagination (admin)");
        
        Page<OrderDto> orders = orderService.getAllOrders(pageable);
        
        return ResponseEntity.ok(orders);
    }
}
