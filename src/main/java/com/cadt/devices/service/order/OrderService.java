package com.cadt.devices.service.order;

import com.cadt.devices.dto.cart.CartDto;
import com.cadt.devices.dto.checkout.CheckoutSummaryRequest;
import com.cadt.devices.dto.checkout.CheckoutSummaryResponse;
import com.cadt.devices.dto.order.*;
import com.cadt.devices.dto.user.AddressDto;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.cart.Cart;
import com.cadt.devices.model.catalog.ProductVariant;
import com.cadt.devices.model.coupon.Coupon;
import com.cadt.devices.model.order.*;
import com.cadt.devices.model.user.Address;
import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.order.OrderAddressRepository;
import com.cadt.devices.repo.order.OrderItemRepository;
import com.cadt.devices.repo.order.OrderRepository;
import com.cadt.devices.service.cart.CartService;
import com.cadt.devices.service.checkout.CheckoutService;
import com.cadt.devices.service.coupon.CouponService;
import com.cadt.devices.service.payment.PaymentService;
import com.cadt.devices.service.user.AddressService;
import com.cadt.devices.repo.catalog.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderAddressRepository orderAddressRepository;
    private final CartService cartService;
    private final CheckoutService checkoutService;
    private final AddressService addressService;
    private final CouponService couponService;
    private final PaymentService paymentService;
    private final ProductVariantRepository variantRepo;

    @Transactional
    public PlaceOrderResponse placeOrder(String userId, String sessionId, PlaceOrderRequest request) {
        log.info("Placing order for user: {} with request: {}", userId, request);

        try {
            // 1. Get and validate cart
            Cart cart = cartService.getOrCreateCartEntity(userId, sessionId);
            if (cart.getItems().isEmpty()) {
                throw new ApiException("CART_EMPTY", "Cart is empty");
            }

            // 2. Get and validate address
            AddressDto addressDto = addressService.getForUser(userId, request.getAddressId());

            // 3. Get checkout summary
            CheckoutSummaryRequest summaryRequest = new CheckoutSummaryRequest(
                    request.getAddressId(),
                    request.getCouponCode(),
                    request.getPaymentMethod()
            );
            
            CheckoutSummaryResponse summary = checkoutService.summarize(userId, sessionId, summaryRequest);

            // 4. Verify payment if Razorpay
            if ("RAZORPAY".equals(request.getPaymentMethod())) {
                if (request.getRazorpayOrderId() == null || request.getRazorpayPaymentId() == null || 
                    request.getRazorpaySignature() == null) {
                    throw new ApiException("RAZORPAY_DETAILS_REQUIRED", "Razorpay payment details are required");
                }
                
                // Verify payment signature
                var verifyRequest = com.cadt.devices.dto.payment.VerifyPaymentRequest.builder()
                        .razorpayOrderId(request.getRazorpayOrderId())
                        .razorpayPaymentId(request.getRazorpayPaymentId())
                        .razorpaySignature(request.getRazorpaySignature())
                        .build();
                
                var verifyResponse = paymentService.verifyPayment(verifyRequest);
                if (!verifyResponse.isVerified()) {
                    throw new ApiException("PAYMENT_VERIFICATION_FAILED", "Payment verification failed");
                }
            }

            // 5. Create order
            User user = new User();
            user.setId(userId);
            
            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.CREATED)
                    .subtotal(summary.subtotal())
                    .discountTotal(summary.discount())
                    .taxTotal(summary.tax())
                    .shippingTotal(summary.shipping())
                    .grandTotal(summary.grandTotal())
                    .currency("INR")
                    .paymentStatus("RAZORPAY".equals(request.getPaymentMethod()) ? 
                            PaymentStatus.PAID : PaymentStatus.PENDING)
                    .paymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()))
                    .razorpayOrderId(request.getRazorpayOrderId())
                    .razorpayPaymentId(request.getRazorpayPaymentId())
                    .razorpaySignature(request.getRazorpaySignature())
                    .codFlag("COD".equals(request.getPaymentMethod()))
                    .appliedCoupon(summary.appliedCoupon() != null ? 
                            couponService.getCouponEntityByCode(summary.appliedCoupon().getCode()) : null)
                    .orderNotes(request.getOrderNotes())
                    .deliveryInstructions(request.getDeliveryInstructions())
                    .estimatedDeliveryDate(Instant.now().plus(3, ChronoUnit.DAYS))
                    .build();

            final Order savedOrder = orderRepository.save(order);

            // 6. Create order items
            List<OrderItem> orderItems = summary.items().stream()
                    .map(item -> {
                        ProductVariant variant = item.getVariantId() != null ?
                                variantRepo.findById(item.getVariantId()).orElse(null) : null;
                        return OrderItem.builder()
                            .order(savedOrder)
                            .variant(variant)
                            .title(item.getProduct() != null ? item.getProduct().getTitle() : "Product")
                            .sku(variant != null ? variant.getSku() : null)
                            .quantity(item.getQuantity())
                            .unitPrice(item.getPriceSnapshot())
                            .totalPrice(item.getPriceSnapshot().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .taxRate(item.getTaxRateSnapshot())
                            .taxAmount(item.getTaxRateSnapshot() != null ? 
                                    item.getPriceSnapshot().multiply(item.getTaxRateSnapshot())
                                            .multiply(BigDecimal.valueOf(item.getQuantity())) : BigDecimal.ZERO)
                            .productSnapshot(createProductSnapshot(item))
                            .build();
                    })
                    .collect(Collectors.toList());

            orderItemRepository.saveAll(orderItems);

            // 7. Create order addresses
            OrderAddress shippingAddress = OrderAddress.builder()
                    .order(savedOrder)
                    .type(AddressType.SHIPPING)
                    .name(addressDto.name())
                    .phone(addressDto.phone())
                    .line1(addressDto.line1())
                    .line2(addressDto.line2())
                    .city(addressDto.city())
                    .state(addressDto.state())
                    .country(addressDto.country())
                    .pincode(addressDto.pincode())
                    .build();

            orderAddressRepository.save(shippingAddress);

            // 8. Clear both user and session carts to be safe
            cartService.clearAllCartsFor(userId, sessionId);

            // 9. Update order status to PAID if payment was successful
            if ("RAZORPAY".equals(request.getPaymentMethod())) {
                savedOrder.setStatus(OrderStatus.PAID);
                orderRepository.save(savedOrder);
            }

            log.info("Order placed successfully: {}", savedOrder.getId());

            return PlaceOrderResponse.builder()
                    .orderId(savedOrder.getId())
                    .message("Order placed successfully")
                    .success(true)
                    .status(savedOrder.getStatus().name())
                    .paymentStatus(savedOrder.getPaymentStatus().name())
                    .build();

        } catch (Exception e) {
            log.error("Failed to place order: {}", e.getMessage(), e);
            throw new ApiException("ORDER_PLACEMENT_FAILED", "Failed to place order: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getUserOrders(String userId) {
        log.debug("Getting orders for user: {}", userId);
        
        User user = new User();
        user.setId(userId);
        
        List<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user);
        return orders.stream()
                .map(this::toOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getUserOrders(String userId, Pageable pageable) {
        log.debug("Getting orders for user: {} with pagination", userId);
        
        User user = new User();
        user.setId(userId);
        
        Page<Order> orders = orderRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return orders.map(this::toOrderDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(String orderId, String userId) {
        log.debug("Getting order: {} for user: {}", orderId, userId);
        
        User user = new User();
        user.setId(userId);
        
        Order order = orderRepository.findByUserAndId(user, orderId)
                .orElseThrow(() -> new ApiException("ORDER_NOT_FOUND", "Order not found"));
        
        return toOrderDto(order);
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        log.debug("Getting all orders");
        
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::toOrderDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.debug("Getting all orders with pagination");
        
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::toOrderDto);
    }

    private OrderDto toOrderDto(Order order) {
        List<OrderItemDto> items = orderItemRepository.findByOrderOrderByCreatedAtAsc(order)
                .stream()
                .map(this::toOrderItemDto)
                .collect(Collectors.toList());

        List<OrderAddressDto> addresses = orderAddressRepository.findByOrder(order)
                .stream()
                .map(this::toOrderAddressDto)
                .collect(Collectors.toList());

        return OrderDto.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .discountTotal(order.getDiscountTotal())
                .taxTotal(order.getTaxTotal())
                .shippingTotal(order.getShippingTotal())
                .grandTotal(order.getGrandTotal())
                .currency(order.getCurrency())
                .paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod())
                .razorpayOrderId(order.getRazorpayOrderId())
                .razorpayPaymentId(order.getRazorpayPaymentId())
                .codFlag(order.getCodFlag())
                .appliedCouponCode(order.getAppliedCoupon() != null ? order.getAppliedCoupon().getCode() : null)
                .orderNotes(order.getOrderNotes())
                .deliveryInstructions(order.getDeliveryInstructions())
                .estimatedDeliveryDate(order.getEstimatedDeliveryDate())
                .actualDeliveryDate(order.getActualDeliveryDate())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(items)
                .addresses(addresses)
                .build();
    }

    private OrderItemDto toOrderItemDto(OrderItem item) {
        return OrderItemDto.builder()
                .id(item.getId())
                .variantId(item.getVariant() != null ? item.getVariant().getId() : null)
                .title(item.getTitle())
                .sku(item.getSku())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .taxRate(item.getTaxRate())
                .taxAmount(item.getTaxAmount())
                .productSnapshot(item.getProductSnapshot())
                .build();
    }

    private OrderAddressDto toOrderAddressDto(OrderAddress address) {
        return OrderAddressDto.builder()
                .id(address.getId())
                .type(address.getType())
                .name(address.getName())
                .phone(address.getPhone())
                .line1(address.getLine1())
                .line2(address.getLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .pincode(address.getPincode())
                .build();
    }

    private String createProductSnapshot(com.cadt.devices.dto.cart.CartItemDto item) {
        // Create a JSON snapshot of product details at time of order
        // This is useful for order history even if product details change later
        return String.format("{\"productId\":\"%s\",\"title\":\"%s\",\"brand\":\"%s\",\"category\":\"%s\"}",
                item.getProduct() != null ? item.getProduct().getId() : "",
                item.getProduct() != null ? item.getProduct().getTitle() : "Product",
                item.getProduct() != null && item.getProduct().getBrand() != null ? item.getProduct().getBrand().getName() : "",
                item.getProduct() != null && item.getProduct().getCategory() != null ? item.getProduct().getCategory().getName() : "");
    }
}
