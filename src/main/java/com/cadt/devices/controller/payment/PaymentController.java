package com.cadt.devices.controller.payment;

import com.cadt.devices.dto.payment.CreateRazorpayOrderRequest;
import com.cadt.devices.dto.payment.CreateRazorpayOrderResponse;
import com.cadt.devices.dto.payment.VerifyPaymentRequest;
import com.cadt.devices.dto.payment.VerifyPaymentResponse;
import com.cadt.devices.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/razorpay/order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CreateRazorpayOrderResponse> createRazorpayOrder(
            @Valid @RequestBody CreateRazorpayOrderRequest request) {
        
        log.info("Creating Razorpay order for amount: {} {}", request.getAmount(), request.getCurrency());
        
        CreateRazorpayOrderResponse response = paymentService.createOrder(request);
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/razorpay/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<VerifyPaymentResponse> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        
        log.info("Verifying payment for order: {}", request.getRazorpayOrderId());
        
        VerifyPaymentResponse response = paymentService.verifyPayment(request);
        
        return ResponseEntity.ok(response);
    }
}
