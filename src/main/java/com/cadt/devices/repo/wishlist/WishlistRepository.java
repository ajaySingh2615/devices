package com.cadt.devices.repo.wishlist;

import com.cadt.devices.model.wishlist.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, String> {

    // Find wishlist by user ID
    Optional<Wishlist> findByUserId(String userId);

    // Check if wishlist exists for user
    boolean existsByUserId(String userId);

    // Delete wishlist by user ID
    void deleteByUserId(String userId);
}
