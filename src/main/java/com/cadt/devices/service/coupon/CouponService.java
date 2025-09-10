package com.cadt.devices.service.coupon;

import com.cadt.devices.dto.coupon.*;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.coupon.Coupon;
import com.cadt.devices.model.coupon.CouponType;
import com.cadt.devices.model.coupon.CouponUsage;
import com.cadt.devices.repo.coupon.CouponRepository;
import com.cadt.devices.repo.coupon.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepo;
    private final CouponUsageRepository couponUsageRepo;

    @PostConstruct
    @Transactional
    public void initializeCoupons() {
        log.info("=== INITIALIZING COUPONS ===");
        try {
            // Check if WELCOME10 coupon exists
            Optional<Coupon> existingCoupon = couponRepo.findByCodeAndIsActiveTrue("WELCOME10");
            if (existingCoupon.isEmpty()) {
                log.info("Creating WELCOME10 coupon...");
                
                Instant now = Instant.now();
                Instant endTime = now.plusSeconds(365 * 24 * 60 * 60); // 1 year from now
                
                Coupon welcomeCoupon = Coupon.builder()
                        .code("WELCOME10")
                        .name("Welcome Discount")
                        .description("Get 10% off on your first order")
                        .type(CouponType.PERCENTAGE)
                        .value(new BigDecimal("10.00"))
                        .minOrderAmount(new BigDecimal("1000.00"))
                        .maxDiscountAmount(new BigDecimal("500.00"))
                        .startAt(now)
                        .endAt(endTime)
                        .usageLimit(1000)
                        .perUserLimit(1)
                        .isActive(true)
                        .build();
                
                couponRepo.save(welcomeCoupon);
                log.info("WELCOME10 coupon created successfully");
            } else {
                log.info("WELCOME10 coupon already exists");
            }
        } catch (Exception e) {
            log.error("Error initializing coupons: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public CouponApplicationResult applyCoupon(String code, BigDecimal orderAmount, String userId) {
        log.info("=== COUPON SERVICE - APPLY COUPON ===");
        log.info("Coupon Code: {}", code);
        log.info("Order Amount: {}", orderAmount);
        log.info("User ID: {}", userId);
        log.info("Current Time: {}", Instant.now());

        // Find active coupon
        log.info("Searching for coupon with code: {}", code);
        Coupon coupon = couponRepo.findActiveCouponByCode(code, Instant.now())
                .orElseThrow(() -> {
                    log.error("Coupon not found: {}", code);
                    return new ApiException("COUPON_NOT_FOUND", "Invalid or expired coupon code");
                });
        
        log.info("Found coupon: {} - {}", coupon.getCode(), coupon.getName());

        // Check if coupon is applicable
        if (!coupon.isApplicable(orderAmount)) {
            if (coupon.getMinOrderAmount() != null && orderAmount.compareTo(coupon.getMinOrderAmount()) < 0) {
                throw new ApiException("COUPON_MIN_ORDER", 
                    String.format("Minimum order amount of ₹%s required", coupon.getMinOrderAmount()));
            }
            throw new ApiException("COUPON_NOT_APPLICABLE", "Coupon is not applicable for this order");
        }

        // Check usage limits
        if (coupon.getUsageLimit() > 0) {
            Integer totalUsage = couponUsageRepo.countByCouponId(coupon.getId());
            if (totalUsage >= coupon.getUsageLimit()) {
                throw new ApiException("COUPON_LIMIT_EXCEEDED", "Coupon usage limit exceeded");
            }
        }

        // Check per-user limit
        if (coupon.getPerUserLimit() > 0 && userId != null) {
            Integer userUsage = couponUsageRepo.countByCouponIdAndUserId(coupon.getId(), userId);
            if (userUsage >= coupon.getPerUserLimit()) {
                throw new ApiException("COUPON_USER_LIMIT_EXCEEDED", 
                    String.format("You can use this coupon only %d time(s)", coupon.getPerUserLimit()));
            }
        }

        // Calculate discount
        BigDecimal discountAmount = coupon.calculateDiscount(orderAmount);
        BigDecimal finalAmount = orderAmount.subtract(discountAmount);

        log.info("Coupon applied successfully: {} - discount: ₹{}", code, discountAmount);

        return CouponApplicationResult.builder()
                .success(true)
                .message("Coupon applied successfully")
                .coupon(toCouponDto(coupon))
                .discountAmount(discountAmount)
                .originalAmount(orderAmount)
                .finalAmount(finalAmount)
                .build();
    }

    @Transactional(readOnly = true)
    public CouponApplicationResult validateCoupon(String code, BigDecimal orderAmount, String userId) {
        try {
            return applyCoupon(code, orderAmount, userId);
        } catch (ApiException e) {
            return CouponApplicationResult.builder()
                    .success(false)
                    .message(e.getMessage())
                    .originalAmount(orderAmount)
                    .finalAmount(orderAmount)
                    .build();
        }
    }

    @Transactional
    public void recordCouponUsage(String couponId, String userId, String orderId, 
                                 BigDecimal discountAmount, BigDecimal orderAmount) {
        CouponUsage usage = CouponUsage.builder()
                .couponId(couponId)
                .userId(userId)
                .orderId(orderId)
                .discountAmount(discountAmount)
                .orderAmount(orderAmount)
                .build();

        couponUsageRepo.save(usage);
        log.info("Recorded coupon usage: {} by user: {} for order: {}", couponId, userId, orderId);
    }

    @Transactional(readOnly = true)
    public List<CouponDto> getActiveCoupons() {
        List<Coupon> coupons = couponRepo.findActiveCoupons(Instant.now());
        return coupons.stream()
                .map(this::toCouponDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponDto getCouponByCode(String code) {
        Coupon coupon = couponRepo.findActiveCouponByCode(code, Instant.now())
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
        return toCouponDto(coupon);
    }

    @Transactional(readOnly = true)
    public Coupon getCouponEntityByCode(String code) {
        return couponRepo.findActiveCouponByCode(code, Instant.now())
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
    }

    public CouponDto toCouponDto(Coupon coupon) {
        return CouponDto.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .name(coupon.getName())
                .description(coupon.getDescription())
                .type(coupon.getType())
                .value(coupon.getValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .startAt(coupon.getStartAt())
                .endAt(coupon.getEndAt())
                .usageLimit(coupon.getUsageLimit())
                .perUserLimit(coupon.getPerUserLimit())
                .isActive(coupon.isActive())
                .createdAt(coupon.getCreatedAt())
                .build();
    }

}
