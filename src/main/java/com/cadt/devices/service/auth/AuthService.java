package com.cadt.devices.service.auth;

import com.cadt.devices.dto.auth.*;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.token.PasswordResetToken;
import com.cadt.devices.model.token.RefreshToken;
import com.cadt.devices.model.user.Role;
import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.audit.AuditLogRepository;
import com.cadt.devices.repo.tokens.PasswordResetTokenRepository;
import com.cadt.devices.repo.tokens.RefreshTokenRepository;
import com.cadt.devices.repo.user.UserRepository;
import com.cadt.devices.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordResetTokenRepository resetRepo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final AuditLogRepository audits;
    private final GoogleTokenVerifier gtv;
    private final OtpService otp;
    private final long refreshExpDays;

    public AuthService(UserRepository u, RefreshTokenRepository rfr, PasswordResetTokenRepository pr, PasswordEncoder pe, JwtService js,
                       AuditLogRepository ar, GoogleTokenVerifier gtv, OtpService otp, @Value("${security.jwt.refresh-exp-days}") long rd) {
        this.users = u;
        this.refreshRepo = rfr;
        this.resetRepo = pr;
        this.encoder = pe;
        this.jwt = js;
        this.audits = ar;
        this.gtv = gtv;
        this.otp = otp;
        this.refreshExpDays = rd;
    }

    @Transactional
    public AuthResponse register(RegisterRequest req, String ip, String ua) {
        if (users.existsByEmail(req.getEmail())) throw new ApiException("EMAIL_TAKEN", "Email already in use");
        var u = users.save(User.builder().email(req.getEmail())
                .name(req.getName()).phone(req.getPhone())
                .passwordHash(encoder.encode(req.getPassword())).role(Role.CUSTOMER).build());
        var tokens = issue(u, ip, ua);
        audit(u.getId(), "REGISTER", ip);
        return tokens;
    }

    @Transactional
    public AuthResponse login(LoginRequest req, String ip, String ua) {
        var u = users.findByEmail(req.getEmail())
                .filter(x -> x.getPasswordHash() != null &&
                        encoder.matches(req.getPassword(), x.getPasswordHash()))
                .orElseThrow(() -> new ApiException("INVALID_CREDENTIALS", "Email or password incorrect"));
        var tokens = issue(u, ip, ua);
        audit(u.getId(), "LOGIN", ip);
        return tokens;
    }

    @Transactional
    public AuthResponse google(GoogleAuthRequest req, String ip, String ua) {
        var p = gtv.verify(req.getIdToken());
        if (p == null) throw new ApiException("GOOGLE_INVALID", "Invalid Google token");
        String email = (String) p.get("email");
        String sub = p.getSubject();
        String name = (String) p.get("name");
        var u = users.findByEmail(email).orElseGet(() -> users.save(User.builder().email(email).googleSub(sub).name(name != null ? name : email).role(Role.CUSTOMER).build()));
        if (u.getGoogleSub() == null) {
            u.setGoogleSub(sub);
            users.save(u);
        }
        var tokens = issue(u, ip, ua);
        audit(u.getId(), "LOGIN_GOOGLE", ip);
        return tokens;
    }

    public void phoneStart(PhoneStartRequest r) {
        otp.sendOtp(r.getPhone());
    }

    @Transactional
    public AuthResponse phoneVerify(PhoneVerifyRequest r, String ip, String ua) {
        if (!otp.verifyOtp(r.getPhone(), r.getOtp())) throw new ApiException("OTP_INVALID", "Incorrect OTP");
        var u = users.findByPhone(r.getPhone()).orElseGet(() -> users.save(User.builder().phone(r.getPhone()).name("User" + r.getPhone()).role(Role.CUSTOMER).build()));
        var tokens = issue(u, ip, ua);
        audit(u.getId(), "LOGIN_PHONE", ip);
        return tokens;
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest r, String ip, String ua) {
        var rt = refreshRepo.findByTokenHash(sha256(r.getRefreshToken())).orElseThrow(() -> new ApiException("REFRESH_INVALID", "Refresh token not found"));
        if (rt.isRevoked() || rt.getExpiresAt().isBefore(Instant.now()))
            throw new ApiException("REFRESH_INVALID", "Refresh token expired/revoked");
        rt.setRevoked(true);
        refreshRepo.save(rt);
        var u = users.findById(rt.getUserId()).orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User missing"));
        var tokens = issue(u, ip, ua);
        audit(u.getId(), "REFRESH", ip);
        return tokens;
    }

    @Transactional
    public void logout(String refreshToken, String userId, String ip) {
        if (refreshToken == null) return;
        refreshRepo.findByTokenHash(sha256(refreshToken)).ifPresent(rt -> {
            rt.setRevoked(true);
            refreshRepo.save(rt);
        });
        audit(userId, "LOGOUT", ip);
    }

    public void forgotPassword(ForgotPasswordRequest r) {
        var u = users.findByEmail(r.getEmail()).orElseThrow(() -> new ApiException("USER_NOT_FOUND", "No user"));
        String token = UUID.randomUUID().toString().replace("-", "");
        resetRepo.save(PasswordResetToken.builder().userId(u.getId())
                .tokenHash(sha256(token)).expiresAt(Instant.now()
                        .plus(30, ChronoUnit.MINUTES)).build()); /* TODO send mail */
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest r) {
        var t = resetRepo.findByTokenHash(sha256(r.getToken())).orElseThrow(() -> new ApiException("TOKEN_INVALID", "Reset token invalid"));
        if (t.isUsed() || t.getExpiresAt().isBefore(Instant.now()))
            throw new ApiException("TOKEN_EXPIRED", "Reset token expired/used");
        var u = users.findById(t.getUserId()).orElseThrow(() -> new ApiException("USER_NOT_FOUND", "No user"));
        u.setPasswordHash(encoder.encode(r.getNewPassword()));
        users.save(u);
        t.setUsed(true);
        resetRepo.save(t);
    }

    private AuthResponse issue(User u, String ip, String ua) {
        if (u.getId() == null) {
            throw new RuntimeException("User ID is null - cannot generate JWT");
        }
        
        // Build claims map, handling null values
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", u.getId());
        claims.put("role", u.getRole().name());
        if (u.getEmail() != null) {
            claims.put("email", u.getEmail());
        }
        if (u.getPhone() != null) {
            claims.put("phone", u.getPhone());
        }
        
        String access = jwt.generate(u.getId(), claims);
        String refresh = UUID.randomUUID().toString() + UUID.randomUUID();
        refreshRepo.save(RefreshToken.builder().userId(u.getId()).tokenHash(sha256(refresh))
                .expiresAt(Instant.now().plus(refreshExpDays, ChronoUnit.DAYS)).ip(ip).userAgent(ua).build());
        return new AuthResponse(access, refresh, new AuthUserDto(u.getId(), u.getName(), u.getEmail(), u.getRole().name()));
    }

    private static String sha256(String v) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(v.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void audit(String userId, String action, String ip) {
        audits.save(com.cadt.devices.model.audit.AuditLog.builder().userId(userId).action(action).ip(ip).build());
    }
}
