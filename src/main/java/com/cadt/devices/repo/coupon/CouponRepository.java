package com.cadt.devices.repo.coupon;

import com.cadt.devices.model.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
    
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startAt <= :now AND c.endAt >= :now")
    List<Coupon> findActiveCoupons(@Param("now") Instant now);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startAt <= :now AND c.endAt >= :now AND c.code = :code")
    Optional<Coupon> findActiveCouponByCode(@Param("code") String code, @Param("now") Instant now);
    
}
