package com.cadt.devices.repo.coupon;

import com.cadt.devices.model.coupon.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, String> {
    
    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.couponId = :couponId")
    Integer countByCouponId(@Param("couponId") String couponId);
    
    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.couponId = :couponId AND cu.userId = :userId")
    Integer countByCouponIdAndUserId(@Param("couponId") String couponId, @Param("userId") String userId);
    
    List<CouponUsage> findByUserId(String userId);
    
    List<CouponUsage> findByOrderId(String orderId);
}
