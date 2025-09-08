package com.cadt.devices.dto.review;

import com.cadt.devices.model.review.ReviewStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ReviewDto {
    private String id;
    private String userId;
    private String productId;
    private Integer rating;
    private String title;
    private String content;
    private ReviewStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Related data
    private UserDto user;
}
