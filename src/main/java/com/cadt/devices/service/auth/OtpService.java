package com.cadt.devices.service.auth;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static class R {
        String otp;
        Instant exp;
    }

    private final Map<String, R> store = new ConcurrentHashMap<>();
    private final Random rnd = new Random();

    public void sendOtp(String phone) {
        String otp = String.format("%06d", rnd.nextInt(999999));
        R r = new R();
        r.otp = otp;
        r.exp = Instant.now().plusSeconds(300);
        store.put(phone, r);
        System.out.println("[DEV] OTP " + phone + " = " + otp);
    }

    public boolean verifyOtp(String phone, String otp) {
        var r = store.get(phone);
        return r != null && Instant.now().isBefore(r.exp) && r.otp.equals(otp);
    }
}
