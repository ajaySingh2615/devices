package com.cadt.devices.controller.user;


import com.cadt.devices.dto.user.UpdateProfileRequest;
import com.cadt.devices.dto.user.UserResponse;
import com.cadt.devices.service.user.UserService;
import com.cadt.devices.service.user.VerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;


@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService users;
    private final VerificationService verifications;

    public UserController(UserService users, VerificationService verifications) {
        this.users = users;
        this.verifications = verifications;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Principal p) {
        var u = users.get(p.getName());
        return ResponseEntity.ok(new UserResponse(
                u.getId(), u.getName(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getAvatarUrl(), u.getStatus().name(), u.getGender() != null ? u.getGender().name() : null, u.getCreatedAt(), u.getEmailVerifiedAt(), u.getPhoneVerifiedAt()
        ));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> update(Principal p, @RequestBody UpdateProfileRequest r) {
        var u = users.update(p.getName(), r);
        return ResponseEntity.ok(new UserResponse(
                u.getId(), u.getName(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getAvatarUrl(), u.getStatus().name(), u.getGender() != null ? u.getGender().name() : null, u.getCreatedAt(), u.getEmailVerifiedAt(), u.getPhoneVerifiedAt()
        ));
    }

    // --- Email verification flow ---
    @PatchMapping("/me/email")
    public ResponseEntity<String> requestEmailChange(Principal p, @RequestParam("email") String email) {
        var token = verifications.issueEmailChangeToken(p.getName(), email);
        // TODO: send token via email; for now return token for testing
        return ResponseEntity.ok(token);
    }

    @PutMapping("/me/email/confirm")
    public ResponseEntity<UserResponse> confirmEmail(Principal p, @RequestParam("token") String token) {
        var u = verifications.confirmEmailChange(p.getName(), token);
        return ResponseEntity.ok(new UserResponse(
                u.getId(), u.getName(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getAvatarUrl(), u.getStatus().name(), u.getGender() != null ? u.getGender().name() : null, u.getCreatedAt(), u.getEmailVerifiedAt(), u.getPhoneVerifiedAt()
        ));
    }

    // --- Phone verification flow ---
    @PatchMapping("/me/phone")
    public ResponseEntity<String> requestPhoneChange(Principal p, @RequestParam("phone") String phone) {
        var otp = verifications.issuePhoneOtp(p.getName(), phone);
        // TODO: send OTP via SMS; for now return for testing
        return ResponseEntity.ok(otp);
    }

    @PutMapping("/me/phone/verify")
    public ResponseEntity<UserResponse> confirmPhone(Principal p, @RequestParam("otp") String otp) {
        var u = verifications.confirmPhoneChange(p.getName(), otp);
        return ResponseEntity.ok(new UserResponse(
                u.getId(), u.getName(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getPhone(), u.getRole().name(), u.getAvatarUrl(), u.getStatus().name(), u.getGender() != null ? u.getGender().name() : null, u.getCreatedAt(), u.getEmailVerifiedAt(), u.getPhoneVerifiedAt()
        ));
    }
}