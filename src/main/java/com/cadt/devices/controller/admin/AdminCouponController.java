package com.cadt.devices.controller.admin;

import com.cadt.devices.dto.coupon.*;
import com.cadt.devices.service.coupon.AdminCouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/coupons")
@RequiredArgsConstructor
@Slf4j
public class AdminCouponController {

    private final AdminCouponService adminCouponService;

    @GetMapping
    public ResponseEntity<List<CouponDto>> getAllCoupons() {
        log.info("Admin: Getting all coupons");
        List<CouponDto> coupons = adminCouponService.getAllCoupons();
        return ResponseEntity.ok(coupons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDto> getCouponById(@PathVariable String id) {
        log.info("Admin: Getting coupon by ID: {}", id);
        CouponDto coupon = adminCouponService.getCouponById(id);
        return ResponseEntity.ok(coupon);
    }

    @PostMapping
    public ResponseEntity<CouponDto> createCoupon(@Valid @RequestBody CreateCouponRequest request) {
        log.info("Admin: Creating new coupon: {}", request.getCode());
        CouponDto coupon = adminCouponService.createCoupon(request);
        return ResponseEntity.ok(coupon);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CouponDto> updateCoupon(
            @PathVariable String id,
            @Valid @RequestBody UpdateCouponRequest request) {
        log.info("Admin: Updating coupon: {}", id);
        CouponDto coupon = adminCouponService.updateCoupon(id, request);
        return ResponseEntity.ok(coupon);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable String id) {
        log.info("Admin: Deleting coupon: {}", id);
        adminCouponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<CouponDto> toggleCouponStatus(
            @PathVariable String id,
            @RequestBody ToggleCouponStatusRequest request) {
        log.info("Admin: Toggling coupon status: {} to {}", id, request.isActive());
        log.info("Admin: Request body received: {}", request);
        CouponDto coupon = adminCouponService.toggleCouponStatus(id, request.isActive());
        log.info("Admin: Service returned coupon with isActive: {}", coupon.isActive());
        return ResponseEntity.ok(coupon);
    }

    @GetMapping("/{id}/usage")
    public ResponseEntity<CouponUsageStatsDto> getCouponUsageStats(@PathVariable String id) {
        log.info("Admin: Getting usage stats for coupon: {}", id);
        CouponUsageStatsDto stats = adminCouponService.getCouponUsageStats(id);
        return ResponseEntity.ok(stats);
    }
}
