package com.cadt.devices.service.user;

import com.cadt.devices.model.user.User;
import com.cadt.devices.repo.user.UserRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {
    private final UserRepository userRepository;

    public VerificationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private final Map<String, String> userToEmailToken = new ConcurrentHashMap<>();
    private final Map<String, String> userToPendingEmail = new ConcurrentHashMap<>();
    private final Map<String, String> userToPhoneOtp = new ConcurrentHashMap<>();
    private final Map<String, String> userToPendingPhone = new ConcurrentHashMap<>();

    public String issueEmailChangeToken(String userId, String newEmail) {
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        userToEmailToken.put(userId, token);
        userToPendingEmail.put(userId, newEmail);
        return token;
    }

    public User confirmEmailChange(String userId, String token) {
        String expected = userToEmailToken.get(userId);
        if (expected == null || !expected.equals(token)) {
            throw new IllegalArgumentException("INVALID_TOKEN");
        }
        String email = userToPendingEmail.remove(userId);
        userToEmailToken.remove(userId);
        var u = userRepository.findById(userId).orElseThrow();
        u.setEmail(email);
        u.setEmailVerifiedAt(Instant.now());
        return userRepository.save(u);
    }

    public String issuePhoneOtp(String userId, String newPhone) {
        // generate 6-digit OTP
        String otp = String.valueOf((int)(Math.random()*900000)+100000);
        userToPhoneOtp.put(userId, otp);
        userToPendingPhone.put(userId, newPhone);
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
        u.setPhone(phone);
        u.setPhoneVerifiedAt(Instant.now());
        return userRepository.save(u);
    }
}


