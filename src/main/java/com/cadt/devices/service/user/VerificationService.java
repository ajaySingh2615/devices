package com.cadt.devices.service.user;

import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.user.UserRepository;
import com.cadt.devices.util.PhoneUtil;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.service.common.EmailService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public VerificationService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    private final Map<String, String> userToEmailToken = new ConcurrentHashMap<>();
    private final Map<String, String> userToPendingEmail = new ConcurrentHashMap<>();
    private final Map<String, String> userToPhoneOtp = new ConcurrentHashMap<>();
    private final Map<String, String> userToPendingPhone = new ConcurrentHashMap<>();

    public String issueEmailChangeToken(String userId, String newEmail) {
        var user = userRepository.findById(userId).orElseThrow();
        // If account is Google-linked and no local password, treat email as managed by Google
        if (user.getGoogleSub() != null && (user.getPasswordHash() == null || user.getPasswordHash().isBlank())) {
            throw new ApiException("EMAIL_MANAGED_BY_GOOGLE", "Email changes are managed by Google for this account");
        }
        // block if email belongs to another user
        if (newEmail != null && userRepository.existsByEmail(newEmail)) {
            var existing = userRepository.findByEmail(newEmail).orElse(null);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new ApiException("EMAIL_TAKEN", "Email already in use");
            }
        }
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        userToEmailToken.put(userId, token);
        userToPendingEmail.put(userId, newEmail);
        // fire transactional email (best-effort)
        try {
            emailService.sendEmail(newEmail, "Verify your email", "<p>Your verification code is <b>" + token + "</b></p>");
        } catch (Exception ignored) {}
        return token;
    }

    public User confirmEmailChange(String userId, String token) {
        String expected = userToEmailToken.get(userId);
        if (expected == null || !expected.equals(token)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }
        String email = userToPendingEmail.remove(userId);
        // prevent duplicate emails
        if (email != null && userRepository.existsByEmail(email)) {
            // If the existing user with this email is the same user, allow it
            var existing = userRepository.findByEmail(email).orElse(null);
            if (existing != null && !existing.getId().equals(userId)) {
                throw new ApiException("EMAIL_TAKEN", "Email already in use");
            }
        }
        userToEmailToken.remove(userId);
        var u = userRepository.findById(userId).orElseThrow();
        u.setEmail(email);
        u.setEmailVerifiedAt(Instant.now());
        return userRepository.save(u);
    }

    public String issuePhoneOtp(String userId, String newPhone) {
        // generate 6-digit OTP
        String otp = String.valueOf((int)(Math.random()*900000)+100000);
        String normalized = PhoneUtil.normalizePhone(newPhone);
        // block if phone belongs to another user
        var existing = userRepository.findByPhone(normalized).orElse(null);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new ApiException("PHONE_TAKEN", "Phone number already in use");
        }
        userToPhoneOtp.put(userId, otp);
        userToPendingPhone.put(userId, normalized);
        return otp;
    }

    public User confirmPhoneChange(String userId, String otp) {
        String expected = userToPhoneOtp.get(userId);
        if (expected == null || !expected.equals(otp)) {
            throw new IllegalArgumentException("INVALID_OTP");
        }
        String phone = userToPendingPhone.remove(userId);
        userToPhoneOtp.remove(userId);
        var u = userRepository.findById(userId).orElseThrow();
        // Normalize again (safety) and enforce uniqueness
        String normalized = PhoneUtil.normalizePhone(phone);
        var existing = userRepository.findByPhone(normalized).orElse(null);
        if (existing != null && !existing.getId().equals(userId)) {
            throw new ApiException("PHONE_TAKEN", "Phone number already in use");
        }
        phone = normalized;
        u.setPhone(phone);
        u.setPhoneVerifiedAt(Instant.now());
        return userRepository.save(u);
    }
}


