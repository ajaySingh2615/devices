package com.cadt.devices.model.order;

import com.cadt.devices.model.common.BaseEntity;
import com.cadt.devices.model.coupon.Coupon;
import com.cadt.devices.model.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @Column(name = "discount_total", precision = 10, scale = 2)
    private BigDecimal discountTotal;

    @Column(name = "tax_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal taxTotal;

    @Column(name = "shipping_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal shippingTotal;

    @Column(name = "grand_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal grandTotal;

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Column(name = "razorpay_signature")
    private String razorpaySignature;

    @Column(name = "cod_flag")
    private Boolean codFlag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applied_coupon_id")
    private Coupon appliedCoupon;

    @Column(name = "order_notes", columnDefinition = "TEXT")
    private String orderNotes;

    @Column(name = "delivery_instructions", columnDefinition = "TEXT")
    private String deliveryInstructions;

    @Column(name = "estimated_delivery_date")
    private Instant estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private Instant actualDeliveryDate;
}
