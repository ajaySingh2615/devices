package com.cadt.devices.controller.review;

import com.cadt.devices.dto.review.*;
import com.cadt.devices.service.review.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDto> createReview(
            Authentication authentication,
            @Valid @RequestBody CreateReviewRequest request) {
        
        String userId = authentication.getName();
        log.debug("Creating review: userId={}, request={}", userId, request);
        
        ReviewDto review = reviewService.createReview(userId, request);
        return ResponseEntity.ok(review);
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> updateReview(
            Authentication authentication,
            @PathVariable String reviewId,
            @Valid @RequestBody UpdateReviewRequest request) {
        
        String userId = authentication.getName();
        log.debug("Updating review: userId={}, reviewId={}, request={}", userId, reviewId, request);
        
        ReviewDto review = reviewService.updateReview(userId, reviewId, request);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            Authentication authentication,
            @PathVariable String reviewId) {
        
        String userId = authentication.getName();
        log.debug("Deleting review: userId={}, reviewId={}", userId, reviewId);
        
        reviewService.deleteReview(userId, reviewId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewDto>> getProductReviews(
            @PathVariable String productId,
            Pageable pageable) {
        
        log.debug("Getting product reviews: productId={}", productId);
        
        Page<ReviewDto> reviews = reviewService.getProductReviews(productId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ProductReviewSummaryDto> getProductReviewSummary(
            @PathVariable String productId) {
        
        log.debug("Getting product review summary: productId={}", productId);
        
        ProductReviewSummaryDto summary = reviewService.getProductReviewSummary(productId);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ReviewDto>> getUserReviews(
            Authentication authentication,
            Pageable pageable) {
        
        String userId = authentication.getName();
        log.debug("Getting user reviews: userId={}", userId);
        
        Page<ReviewDto> reviews = reviewService.getUserReviews(userId, pageable);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/my/product/{productId}")
    public ResponseEntity<ReviewDto> getUserReviewForProduct(
            Authentication authentication,
            @PathVariable String productId) {
        
        String userId = authentication.getName();
        log.debug("Getting user review for product: userId={}, productId={}", userId, productId);
        
        ReviewDto review = reviewService.getUserReviewForProduct(userId, productId);
        return ResponseEntity.ok(review);
    }

    // Admin endpoints
    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ReviewDto>> getPendingReviews(Pageable pageable) {
        log.debug("Getting pending reviews for moderation");
        
        Page<ReviewDto> reviews = reviewService.getPendingReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @PatchMapping("/admin/{reviewId}/moderate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewDto> moderateReview(
            @PathVariable String reviewId,
            @Valid @RequestBody ModerateReviewRequest request) {
        
        log.debug("Moderating review: reviewId={}, request={}", reviewId, request);
        
        ReviewDto review = reviewService.moderateReview(reviewId, request);
        return ResponseEntity.ok(review);
    }
}
