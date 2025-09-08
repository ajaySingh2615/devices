package com.cadt.devices.dto.review;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class ProductReviewSummaryDto {
    private String productId;
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingDistribution; // rating -> count
    private List<ReviewDto> recentReviews;
}
