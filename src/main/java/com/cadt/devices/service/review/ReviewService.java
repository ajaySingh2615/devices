package com.cadt.devices.service.review;

import com.cadt.devices.dto.review.*;
import com.cadt.devices.dto.review.UserDto;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.review.Review;
import com.cadt.devices.model.review.ReviewStatus;
import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.review.ReviewRepository;
import com.cadt.devices.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final UserRepository userRepo;

    @Transactional
    public ReviewDto createReview(String userId, CreateReviewRequest request) {
        log.debug("Creating review: userId={}, productId={}, rating={}", 
                userId, request.getProductId(), request.getRating());

        // Check if user has already reviewed this product
        if (reviewRepo.existsByUserIdAndProductId(userId, request.getProductId())) {
            throw new ApiException("REVIEW_ALREADY_EXISTS", "You have already reviewed this product");
        }

        Review review = Review.builder()
                .userId(userId)
                .productId(request.getProductId())
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .status(ReviewStatus.PENDING)
                .build();

        review = reviewRepo.save(review);
        return toReviewDto(review);
    }

    @Transactional
    public ReviewDto updateReview(String userId, String reviewId, UpdateReviewRequest request) {
        log.debug("Updating review: userId={}, reviewId={}", userId, reviewId);

        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ApiException("REVIEW_NOT_FOUND", "Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ApiException("REVIEW_NOT_FOUND", "Review not found");
        }

        if (review.getStatus() != ReviewStatus.PENDING) {
            throw new ApiException("REVIEW_CANNOT_BE_UPDATED", "Only pending reviews can be updated");
        }

        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            review.setContent(request.getContent());
        }

        review = reviewRepo.save(review);
        return toReviewDto(review);
    }

    @Transactional
    public void deleteReview(String userId, String reviewId) {
        log.debug("Deleting review: userId={}, reviewId={}", userId, reviewId);

        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ApiException("REVIEW_NOT_FOUND", "Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ApiException("REVIEW_NOT_FOUND", "Review not found");
        }

        reviewRepo.delete(review);
    }

    public Page<ReviewDto> getProductReviews(String productId, Pageable pageable) {
        log.debug("Getting product reviews: productId={}", productId);

        Page<Review> reviews = reviewRepo.findByProductIdAndStatusOrderByCreatedAtDesc(
                productId, ReviewStatus.APPROVED, pageable);
        
        return reviews.map(this::toReviewDto);
    }

    public Page<ReviewDto> getUserReviews(String userId, Pageable pageable) {
        log.debug("Getting user reviews: userId={}", userId);

        Page<Review> reviews = reviewRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return reviews.map(this::toReviewDto);
    }

    public ReviewDto getUserReviewForProduct(String userId, String productId) {
        log.debug("Getting user review for product: userId={}, productId={}", userId, productId);

        Review review = reviewRepo.findByUserIdAndProductId(userId, productId).orElse(null);
        return review != null ? toReviewDto(review) : null;
    }

    public ProductReviewSummaryDto getProductReviewSummary(String productId) {
        log.debug("Getting product review summary: productId={}", productId);

        Double averageRating = reviewRepo.getAverageRatingByProductId(productId);
        Long totalReviews = reviewRepo.getReviewCountByProductId(productId);
        
        List<Object[]> ratingDistribution = reviewRepo.getRatingDistributionByProductId(productId);
        Map<Integer, Long> distribution = new HashMap<>();
        for (Object[] row : ratingDistribution) {
            distribution.put((Integer) row[0], (Long) row[1]);
        }

        // Get recent reviews (limit to 5)
        Page<Review> recentReviews = reviewRepo.findByProductIdAndStatusOrderByCreatedAtDesc(
                productId, ReviewStatus.APPROVED, Pageable.ofSize(5));
        
        List<ReviewDto> recentReviewDtos = recentReviews.getContent().stream()
                .map(this::toReviewDto)
                .collect(Collectors.toList());

        return ProductReviewSummaryDto.builder()
                .productId(productId)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .ratingDistribution(distribution)
                .recentReviews(recentReviewDtos)
                .build();
    }

    // Admin methods
    public Page<ReviewDto> getPendingReviews(Pageable pageable) {
        log.debug("Getting pending reviews for moderation");

        Page<Review> reviews = reviewRepo.findByStatusOrderByCreatedAtDesc(ReviewStatus.PENDING, pageable);
        return reviews.map(this::toReviewDto);
    }

    @Transactional
    public ReviewDto moderateReview(String reviewId, ModerateReviewRequest request) {
        log.debug("Moderating review: reviewId={}, status={}", reviewId, request.getStatus());

        Review review = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new ApiException("REVIEW_NOT_FOUND", "Review not found"));

        review.setStatus(request.getStatus());
        review = reviewRepo.save(review);

        return toReviewDto(review);
    }

    private ReviewDto toReviewDto(Review review) {
        User user = userRepo.findById(review.getUserId()).orElse(null);
        UserDto userDto = user != null ? toUserDto(user) : null;

        return ReviewDto.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .productId(review.getProductId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .status(review.getStatus())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .user(userDto)
                .build();
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
