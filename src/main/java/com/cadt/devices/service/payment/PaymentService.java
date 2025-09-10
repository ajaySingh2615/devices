package com.cadt.devices.service.payment;

import com.cadt.devices.dto.payment.CreateRazorpayOrderRequest;
import com.cadt.devices.dto.payment.CreateRazorpayOrderResponse;
import com.cadt.devices.dto.payment.VerifyPaymentRequest;
import com.cadt.devices.dto.payment.VerifyPaymentResponse;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final RazorpayClient razorpayClient;
    
    @Value("${razorpay.key.secret}")
    private String secretKey;

    public CreateRazorpayOrderResponse createOrder(CreateRazorpayOrderRequest request) {
        try {
            log.info("Creating Razorpay order for amount: {} {}", request.getAmount(), request.getCurrency());

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue()); // Convert to paise
            orderRequest.put("currency", request.getCurrency());
            orderRequest.put("receipt", request.getReceipt() != null ? request.getReceipt() : "receipt_" + System.currentTimeMillis());
            
            if (request.getNotes() != null) {
                JSONObject notes = new JSONObject();
                notes.put("notes", request.getNotes());
                orderRequest.put("notes", notes);
            }

            Order order = razorpayClient.orders.create(orderRequest);
            
            log.info("Razorpay order created successfully: {}", order.get("id").toString());

            return CreateRazorpayOrderResponse.builder()
                    .id(order.get("id") != null ? order.get("id").toString() : null)
                    .entity(order.get("entity") != null ? order.get("entity").toString() : null)
                    .amount(Long.parseLong(order.get("amount").toString()))
                    .amountPaid(order.get("amount_paid") != null ? order.get("amount_paid").toString() : "0")
                    .amountDue(order.get("amount_due") != null ? order.get("amount_due").toString() : "0")
                    .currency(order.get("currency") != null ? order.get("currency").toString() : "INR")
                    .receipt(order.get("receipt") != null ? order.get("receipt").toString() : null)
                    .status(order.get("status") != null ? order.get("status").toString() : "created")
                    .attempts(Long.parseLong(order.get("attempts").toString()))
                    .notes(order.get("notes") != null ? order.get("notes").toString() : null)
                    .createdAt(System.currentTimeMillis() / 1000) // Use current timestamp in seconds
                    .build();

        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            throw new RuntimeException("Failed to create payment order: " + e.getMessage(), e);
        }
    }

    public VerifyPaymentResponse verifyPayment(VerifyPaymentRequest request) {
        try {
            log.info("Verifying payment for order: {}", request.getRazorpayOrderId());

            // Create the signature string
            String signatureString = request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId();
            
            // Get the secret key from configuration
            String secret = secretKey;
            
            // Generate HMAC SHA256 signature
            String expectedSignature = calculateHMACHex(signatureString, secret);
            
            boolean isVerified = expectedSignature.equalsIgnoreCase(request.getRazorpaySignature());
            
            log.info("Payment verification result: {}", isVerified);

            return VerifyPaymentResponse.builder()
                    .verified(isVerified)
                    .message(isVerified ? "Payment verified successfully" : "Payment verification failed")
                    .orderId(request.getRazorpayOrderId())
                    .build();

        } catch (Exception e) {
            log.error("Failed to verify payment", e);
            return VerifyPaymentResponse.builder()
                    .verified(false)
                    .message("Payment verification failed: " + e.getMessage())
                    .orderId(request.getRazorpayOrderId())
                    .build();
        }
    }

    private String calculateHMACHex(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] signature = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * signature.length);
        for (byte b : signature) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}
