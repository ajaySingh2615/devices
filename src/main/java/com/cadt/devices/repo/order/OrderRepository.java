package com.cadt.devices.repo.order;

import com.cadt.devices.model.order.Order;
import com.cadt.devices.model.order.OrderStatus;
import com.cadt.devices.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Order> findByUserAndStatusOrderByCreatedAtDesc(User user, OrderStatus status);

    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);

    Optional<Order> findByRazorpayPaymentId(String razorpayPaymentId);

    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.id = :orderId")
    Optional<Order> findByUserAndId(@Param("user") User user, @Param("orderId") String orderId);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :fromDate AND o.createdAt <= :toDate")
    List<Order> findByStatusAndDateRange(@Param("status") OrderStatus status, 
                                        @Param("fromDate") Instant fromDate, 
                                        @Param("toDate") Instant toDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.status = :status")
    Long countByUserAndStatus(@Param("user") User user, @Param("status") OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.createdAt >= :fromDate AND o.createdAt <= :toDate ORDER BY o.createdAt DESC")
    List<Order> findByDateRange(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
