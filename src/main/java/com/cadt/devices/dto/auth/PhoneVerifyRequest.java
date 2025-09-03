package com.cadt.devices.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PhoneVerifyRequest {

    @NotBlank
    private String phone;
    @NotBlank
    private String otp;
}
