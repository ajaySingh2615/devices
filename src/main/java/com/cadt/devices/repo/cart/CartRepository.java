package com.cadt.devices.repo.cart;

import com.cadt.devices.model.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, String> {

    // Find cart by user ID (for logged-in users)
    Optional<Cart> findByUserId(String userId);

    // Find cart by session ID (for anonymous users)
    Optional<Cart> findBySessionId(String sessionId);

    // Find cart by user ID or session ID
    @Query("SELECT c FROM Cart c WHERE c.userId = :userId OR c.sessionId = :sessionId")
    Optional<Cart> findByUserIdOrSessionId(@Param("userId") String userId, @Param("sessionId") String sessionId);

    // Check if cart exists for user
    boolean existsByUserId(String userId);

    // Check if cart exists for session
    boolean existsBySessionId(String sessionId);

    // Delete cart by user ID
    void deleteByUserId(String userId);

    // Delete cart by session ID
    void deleteBySessionId(String sessionId);
}
