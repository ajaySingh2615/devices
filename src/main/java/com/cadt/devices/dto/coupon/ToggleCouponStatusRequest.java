package com.cadt.devices.dto.coupon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ToggleCouponStatusRequest {
    @JsonProperty("isActive")
    private boolean isActive;

    @JsonCreator
    public ToggleCouponStatusRequest(@JsonProperty(value = "isActive", required = true) boolean isActive) {
        this.isActive = isActive;
    }
}
