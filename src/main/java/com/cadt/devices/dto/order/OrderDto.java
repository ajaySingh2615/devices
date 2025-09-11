package com.cadt.devices.dto.order;

import com.cadt.devices.model.order.OrderStatus;
import com.cadt.devices.model.order.PaymentStatus;
import com.cadt.devices.model.order.PaymentMethod;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    private String id;

    private String userId;

    private OrderStatus status;

    private BigDecimal subtotal;

    private BigDecimal discountTotal;

    private BigDecimal taxTotal;

    private BigDecimal shippingTotal;

    private BigDecimal grandTotal;

    private String currency;

    private PaymentStatus paymentStatus;

    private PaymentMethod paymentMethod;

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private Boolean codFlag;

    private String appliedCouponCode;

    private String orderNotes;

    private String deliveryInstructions;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant estimatedDeliveryDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant actualDeliveryDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;

    private List<OrderItemDto> items;

    private List<OrderAddressDto> addresses;
}
