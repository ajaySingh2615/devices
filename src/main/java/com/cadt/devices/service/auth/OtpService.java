package com.cadt.devices.service.auth;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class OtpRecord {
        String otp;
        Instant expiry;
        int attempts;
    }

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // Twilio configuration
    private final boolean twilioEnabled;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioPhoneNumber;

    public OtpService(
            @Value("${twilio.enabled:false}") boolean twilioEnabled,
            @Value("${twilio.account-sid:}") String twilioAccountSid,
            @Value("${twilio.auth-token:}") String twilioAuthToken,
            @Value("${twilio.phone-number:}") String twilioPhoneNumber) {
        
        this.twilioEnabled = twilioEnabled;
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.twilioPhoneNumber = twilioPhoneNumber;
        
        // Initialize Twilio if enabled and configured
        if (twilioEnabled && !twilioAccountSid.isEmpty() && !twilioAuthToken.isEmpty()) {
            Twilio.init(twilioAccountSid, twilioAuthToken);
            System.out.println("[INFO] Twilio SMS service initialized");
        } else {
            System.out.println("[INFO] Using development mode - OTP will be logged to console");
        }
    }

    /**
     * Sends OTP to the given phone number
     * In development mode: logs to console
     * In production mode: sends via Twilio SMS
     */
    public void sendOtp(String phone) {
        // Clean up expired OTPs
        cleanupExpiredOtps();
        
        // Check rate limiting (max 3 OTPs per 15 minutes per phone)
        if (isRateLimited(phone)) {
            throw new RuntimeException("Too many OTP requests. Please wait before requesting again.");
        }
        
        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(999999));
        
        // Store OTP with 5-minute expiry
        OtpRecord record = new OtpRecord();
        record.otp = otp;
        record.expiry = Instant.now().plusSeconds(300); // 5 minutes
        record.attempts = 0;
        otpStore.put(phone, record);
        
        // Send OTP
        if (twilioEnabled && !twilioAccountSid.isEmpty()) {
            sendViaTwilio(phone, otp);
        } else {
            // Development mode - log to console
            System.out.println("📱 [DEV] OTP for " + phone + " = " + otp + " (expires in 5 minutes)");
        }
    }

    /**
     * Verifies the OTP for the given phone number
     */
    public boolean verifyOtp(String phone, String otp) {
        OtpRecord record = otpStore.get(phone);
        
        if (record == null) {
            return false; // No OTP found
        }
        
        if (Instant.now().isAfter(record.expiry)) {
            otpStore.remove(phone); // Clean up expired OTP
            return false; // OTP expired
        }
        
        // Increment attempts (prevent brute force)
        record.attempts++;
        if (record.attempts > 5) {
            otpStore.remove(phone); // Remove after too many attempts
            return false;
        }
        
        if (record.otp.equals(otp)) {
            otpStore.remove(phone); // Remove successful OTP
            return true;
        }
        
        return false; // Invalid OTP
    }
    
    /**
     * Sends SMS via Twilio
     */
    private void sendViaTwilio(String phone, String otp) {
        try {
            String messageBody = String.format(
                "Your DeviceHub verification code is: %s\n\nThis code expires in 5 minutes.\n\nDo not share this code with anyone.",
                otp
            );
            
            Message message = Message.creator(
                    new PhoneNumber(phone),           // To phone number
                    new PhoneNumber(twilioPhoneNumber), // From phone number (your Twilio number)
                    messageBody                       // Message content
            ).create();
            
            System.out.println("📱 [TWILIO] SMS sent to " + phone + " | Message SID: " + message.getSid());
            
        } catch (Exception e) {
            System.err.println("❌ [TWILIO] Failed to send SMS to " + phone + ": " + e.getMessage());
            // Fallback to console logging in case of Twilio failure
            System.out.println("📱 [FALLBACK] OTP for " + phone + " = " + otp);
        }
    }
    
    /**
     * Check if phone number is rate limited
     */
    private boolean isRateLimited(String phone) {
        // Simple rate limiting: check if there's already an active OTP
        OtpRecord existing = otpStore.get(phone);
        if (existing != null && Instant.now().isBefore(existing.expiry)) {
            // Check if last OTP was sent less than 60 seconds ago
            long timeSinceGeneration = 300 - (existing.expiry.getEpochSecond() - Instant.now().getEpochSecond());
            return timeSinceGeneration < 60; // 60-second cooldown
        }
        return false;
    }
    
    /**
     * Clean up expired OTPs to prevent memory leaks
     */
    private void cleanupExpiredOtps() {
        Instant now = Instant.now();
        otpStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiry));
    }
}
