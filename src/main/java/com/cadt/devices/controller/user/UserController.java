package com.cadt.devices.controller.user;


import com.cadt.devices.dto.user.UpdateProfileRequest;
import com.cadt.devices.dto.user.UserResponse;
import com.cadt.devices.service.user.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal p) {
        var u = users.get(p.getName());
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getAvatarUrl(), u.getStatus().name(), u.getCreatedAt()));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> update(Principal p, @RequestBody UpdateProfileRequest r) {
        var u = users.update(p.getName(), r);
        return ResponseEntity.ok(new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getAvatarUrl(), u.getStatus().name(), u.getCreatedAt()));
    }
}