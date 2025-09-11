package com.cadt.devices.controller.order;

import com.cadt.devices.dto.order.OrderDto;
import com.cadt.devices.dto.order.PlaceOrderRequest;
import com.cadt.devices.dto.order.PlaceOrderResponse;
import com.cadt.devices.dto.order.UpdateOrderStatusRequest;
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
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<OrderDto> getOrderById(
            @PathVariable String orderId,
            Authentication authentication) {
        if (authentication != null && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            log.debug("Admin fetching order: {}", orderId);
            return ResponseEntity.ok(orderService.getOrderByIdAdmin(orderId));
        }
        String userId = authentication != null ? authentication.getName() : null;
        log.debug("Customer fetching order: {} for user: {}", orderId, userId);
        return ResponseEntity.ok(orderService.getOrderById(orderId, userId));
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

    @PatchMapping("/admin/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDto> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusRequest r) {
        log.info("Admin: updating status for order {} to {}", orderId, r.getStatus());
        return ResponseEntity.ok(orderService.updateStatus(orderId, r.getStatus()));
    }
}
