package com.cadt.devices.controller.coupon;

import com.cadt.devices.dto.coupon.*;
import com.cadt.devices.service.coupon.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponDto>> getActiveCoupons() {
        log.debug("Getting active coupons");
        List<CouponDto> coupons = couponService.getActiveCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CouponDto> getCouponByCode(@PathVariable String code) {
        log.debug("Getting coupon by code: {}", code);
        CouponDto coupon = couponService.getCouponByCode(code);
        return ResponseEntity.ok(coupon);
    }

    @PostMapping("/validate")
    public ResponseEntity<CouponApplicationResult> validateCoupon(
            @Valid @RequestBody ApplyCouponRequest request,
            @RequestParam BigDecimal orderAmount,
            Authentication authentication) {
        
        log.debug("Validating coupon: {} for order amount: {}", request.getCode(), orderAmount);
        
        String userId = authentication != null ? authentication.getName() : null;
        CouponApplicationResult result = couponService.validateCoupon(
                request.getCode(), orderAmount, userId);
        
        return ResponseEntity.ok(result);
    }
}
