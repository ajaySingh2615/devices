package com.cadt.devices.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthUserDto {

    private String id;
    private String name;
    private String email;
    private String role;
}
