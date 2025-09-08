package com.cadt.devices.dto.review;

import com.cadt.devices.model.review.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModerateReviewRequest {
    
    @NotNull(message = "Status is required")
    private ReviewStatus status;
}
