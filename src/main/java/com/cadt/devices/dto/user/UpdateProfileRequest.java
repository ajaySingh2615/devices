package com.cadt.devices.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @NotBlank
    private String name;
    private String phone;
    private String avatarUrl;

    // New optional fields
    private String firstName;
    private String lastName;
    private String gender; // MALE, FEMALE, UNSPECIFIED
}
