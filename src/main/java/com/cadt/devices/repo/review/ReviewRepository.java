package com.cadt.devices.repo.review;

import com.cadt.devices.model.review.Review;
import com.cadt.devices.model.review.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {

    // Find reviews by product ID (approved only)
    Page<Review> findByProductIdAndStatusOrderByCreatedAtDesc(String productId, ReviewStatus status, Pageable pageable);

    // Find all reviews by product ID (for admin)
    Page<Review> findByProductIdOrderByCreatedAtDesc(String productId, Pageable pageable);

    // Find reviews by user ID
    Page<Review> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    // Find review by user ID and product ID
    Optional<Review> findByUserIdAndProductId(String userId, String productId);

    // Check if user has reviewed product
    boolean existsByUserIdAndProductId(String userId, String productId);

    // Find reviews by status (for admin moderation)
    Page<Review> findByStatusOrderByCreatedAtDesc(ReviewStatus status, Pageable pageable);

    // Get average rating for product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Double getAverageRatingByProductId(@Param("productId") String productId);

    // Get review count for product
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED'")
    Long getReviewCountByProductId(@Param("productId") String productId);

    // Get rating distribution for product
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId AND r.status = 'APPROVED' GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") String productId);
}
