package com.cadt.devices.dto.user;

import java.time.Instant;

public record AddressDto(
        String id,
        String name,
        String phone,
        String line1,
        String line2,
        String city,
        String state,
        String country,
        String pincode,
        boolean isDefault,
        Instant createdAt
) {}


