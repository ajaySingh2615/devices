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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCouponService {

    private final CouponRepository couponRepo;
    private final CouponUsageRepository couponUsageRepo;

    @Transactional(readOnly = true)
    public List<CouponDto> getAllCoupons() {
        log.info("Admin: Getting all coupons");
        List<Coupon> coupons = couponRepo.findAll();
        return coupons.stream()
                .map(this::toCouponDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CouponDto getCouponById(String id) {
        log.info("Admin: Getting coupon by ID: {}", id);
        Coupon coupon = couponRepo.findById(id)
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
        return toCouponDto(coupon);
    }

    @Transactional
    public CouponDto createCoupon(CreateCouponRequest request) {
        log.info("Admin: Creating new coupon: {}", request.getCode());
        
        // Check if coupon code already exists
        if (couponRepo.findByCodeAndIsActiveTrue(request.getCode()).isPresent()) {
            throw new ApiException("COUPON_CODE_EXISTS", "Coupon code already exists");
        }
        
        // Validate dates
        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new ApiException("INVALID_DATES", "Start date must be before end date");
        }
        
        // Validate percentage values
        if (request.getType() == CouponType.PERCENTAGE && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException("INVALID_PERCENTAGE", "Percentage discount cannot exceed 100%");
        }
        
        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType())
                .value(request.getValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .usageLimit(request.getUsageLimit())
                .perUserLimit(request.getPerUserLimit())
                .isActive(request.isActive())
                .build();
        
        Coupon savedCoupon = couponRepo.save(coupon);
        log.info("Admin: Created coupon: {} with ID: {}", savedCoupon.getCode(), savedCoupon.getId());
        
        return toCouponDto(savedCoupon);
    }

    @Transactional
    public CouponDto updateCoupon(String id, UpdateCouponRequest request) {
        log.info("Admin: Updating coupon: {}", id);
        
        Coupon coupon = couponRepo.findById(id)
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
        
        // Check if coupon code already exists (excluding current coupon)
        Optional<Coupon> existingCoupon = couponRepo.findByCodeAndIsActiveTrue(request.getCode());
        if (existingCoupon.isPresent() && !existingCoupon.get().getId().equals(id)) {
            throw new ApiException("COUPON_CODE_EXISTS", "Coupon code already exists");
        }
        
        // Validate dates
        if (request.getStartAt().isAfter(request.getEndAt())) {
            throw new ApiException("INVALID_DATES", "Start date must be before end date");
        }
        
        // Validate percentage values
        if (request.getType() == CouponType.PERCENTAGE && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ApiException("INVALID_PERCENTAGE", "Percentage discount cannot exceed 100%");
        }
        
        // Update coupon fields
        coupon.setCode(request.getCode().toUpperCase());
        coupon.setName(request.getName());
        coupon.setDescription(request.getDescription());
        coupon.setType(request.getType());
        coupon.setValue(request.getValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setStartAt(request.getStartAt());
        coupon.setEndAt(request.getEndAt());
        coupon.setUsageLimit(request.getUsageLimit());
        coupon.setPerUserLimit(request.getPerUserLimit());
        coupon.setActive(request.isActive());
        
        Coupon updatedCoupon = couponRepo.save(coupon);
        log.info("Admin: Updated coupon: {} with ID: {}", updatedCoupon.getCode(), updatedCoupon.getId());
        
        return toCouponDto(updatedCoupon);
    }

    @Transactional
    public void deleteCoupon(String id) {
        log.info("Admin: Deleting coupon: {}", id);
        
        Coupon coupon = couponRepo.findById(id)
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
        
        // Check if coupon has been used
        Integer usageCount = couponUsageRepo.countByCouponId(id);
        if (usageCount > 0) {
            throw new ApiException("COUPON_IN_USE", "Cannot delete coupon that has been used");
        }
        
        couponRepo.delete(coupon);
        log.info("Admin: Deleted coupon: {} with ID: {}", coupon.getCode(), coupon.getId());
    }

    @Transactional
    public CouponDto toggleCouponStatus(String id, boolean isActive) {
        log.info("Admin: Toggling coupon status: {} to {}", id, isActive);
        
        Coupon coupon = couponRepo.findById(id)
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
        
        log.info("Admin: Before update - coupon.isActive: {}", coupon.isActive());
        coupon.setActive(isActive);
        log.info("Admin: After setActive - coupon.isActive: {}", coupon.isActive());
        
        Coupon updatedCoupon = couponRepo.save(coupon);
        log.info("Admin: After save - updatedCoupon.isActive: {}", updatedCoupon.isActive());
        
        log.info("Admin: Toggled coupon status: {} to {}", updatedCoupon.getCode(), isActive);
        return toCouponDto(updatedCoupon);
    }

    @Transactional(readOnly = true)
    public CouponUsageStatsDto getCouponUsageStats(String id) {
        log.info("Admin: Getting usage stats for coupon: {}", id);
        
        Coupon coupon = couponRepo.findById(id)
                .orElseThrow(() -> new ApiException("COUPON_NOT_FOUND", "Coupon not found"));
        
        List<CouponUsage> usages = couponUsageRepo.findByCouponId(id);
        
        Integer totalUsage = usages.size();
        BigDecimal totalDiscountGiven = usages.stream()
                .map(CouponUsage::getDiscountAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageOrderValue = usages.stream()
                .map(CouponUsage::getOrderAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(totalUsage, 1)), 2, java.math.RoundingMode.HALF_UP);
        
        Instant firstUsed = usages.stream()
                .map(CouponUsage::getCreatedAt)
                .min(Instant::compareTo)
                .orElse(null);
        
        Instant lastUsed = usages.stream()
                .map(CouponUsage::getCreatedAt)
                .max(Instant::compareTo)
                .orElse(null);
        
        Integer uniqueUsers = (int) usages.stream()
                .map(CouponUsage::getUserId)
                .distinct()
                .count();
        
        Double usageRate = coupon.getUsageLimit() > 0 
                ? (double) totalUsage / coupon.getUsageLimit() * 100 
                : 0.0;
        
        return CouponUsageStatsDto.builder()
                .couponId(coupon.getId())
                .couponCode(coupon.getCode())
                .couponName(coupon.getName())
                .totalUsage(totalUsage)
                .usageLimit(coupon.getUsageLimit())
                .totalDiscountGiven(totalDiscountGiven)
                .averageOrderValue(averageOrderValue)
                .firstUsed(firstUsed)
                .lastUsed(lastUsed)
                .uniqueUsers(uniqueUsers)
                .usageRate(usageRate)
                .build();
    }

    private CouponDto toCouponDto(Coupon coupon) {
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
