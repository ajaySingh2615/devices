package com.cadt.devices.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private String avatarUrl;
}
