package com.cadt.devices.dto.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDto {
    private String id;
    private String name;
    private String email;
    private String avatarUrl;
}
