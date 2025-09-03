package com.cadt.devices.controller.auth;


import com.cadt.devices.dto.auth.*;
import com.cadt.devices.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r, HttpServletRequest h) {
        return ResponseEntity.ok(auth.register(r, h.getRemoteAddr(), h.getHeader("User-Agent")));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest r, HttpServletRequest h) {
        return ResponseEntity.ok(auth.login(r, h.getRemoteAddr(), h.getHeader("User-Agent")));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleAuthRequest r, HttpServletRequest h) {
        return ResponseEntity.ok(auth.google(r, h.getRemoteAddr(), h.getHeader("User-Agent")));
    }

    @PostMapping("/phone/start")
    public ResponseEntity<Void> start(@Valid @RequestBody PhoneStartRequest r) {
        auth.phoneStart(r);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/phone/verify")
    public ResponseEntity<AuthResponse> verify(@Valid @RequestBody PhoneVerifyRequest r, HttpServletRequest h) {
        return ResponseEntity.ok(auth.phoneVerify(r, h.getRemoteAddr(), h.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest r, HttpServletRequest h) {
        return ResponseEntity.ok(auth.refresh(r, h.getRemoteAddr(), h.getHeader("User-Agent")));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "X-Refresh-Token",
            required = false) String rt, HttpServletRequest h) {
        var p = h.getUserPrincipal();
        String uid = p != null ? p.getName() : null;
        auth.logout(rt, uid, h.getRemoteAddr());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<Void> forgot(@Valid @RequestBody ForgotPasswordRequest r) {
        auth.forgotPassword(r);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Void> reset(@Valid @RequestBody ResetPasswordRequest r) {
        auth.resetPassword(r);
        return ResponseEntity.ok().build();
    }
}