package com.cadt.devices.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateAddressRequest(
        @NotBlank String name,
        String phone,
        @NotBlank String line1,
        String line2,
        @NotBlank String city,
        @NotBlank String state,
        @NotBlank String country,
        @NotBlank @Pattern(regexp = "^\\d{6}$") String pincode,
        Boolean isDefault
) {}


