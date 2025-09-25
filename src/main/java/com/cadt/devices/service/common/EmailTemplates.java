package com.cadt.devices.service.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailTemplates {
    private final String siteUrl;

    public EmailTemplates(@Value("${site.url:https://localhost:3000}") String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String welcomeHtml(String name) {
        String safeName = name != null && !name.isBlank() ? name : "there";
        return "<div style='font-family:Inter,Arial,sans-serif'>" +
                "<h2>Welcome to DeviceHub, " + safeName + "!</h2>" +
                "<p>Your account has been created successfully.</p>" +
                "<p><a href='" + siteUrl + "' style='display:inline-block;padding:10px 16px;background:#2563eb;color:#fff;border-radius:8px;text-decoration:none'>Shop now</a></p>" +
                "</div>";
    }

    public String verifyEmailHtml(String code) {
        return "<div style='font-family:Inter,Arial,sans-serif'>" +
                "<h2>Verify your email</h2>" +
                "<p>Your verification code is:</p>" +
                "<div style='font-size:20px;font-weight:700;letter-spacing:2px'>" + code + "</div>" +
                "</div>";
    }

    public String passwordChangedHtml() {
        return "<div style='font-family:Inter,Arial,sans-serif'>" +
                "<h2>Password changed</h2>" +
                "<p>Your password was changed successfully. If this wasn't you, please reset it immediately.</p>" +
                "<p><a href='" + siteUrl + "/auth/forgot-password' style='display:inline-block;padding:10px 16px;background:#ef4444;color:#fff;border-radius:8px;text-decoration:none'>Reset password</a></p>" +
                "</div>";
    }

    public String resetPasswordHtml(String token) {
        String link = siteUrl + "/auth/reset-password?token=" + token;
        return "<div style='font-family:Inter,Arial,sans-serif'>" +
                "<h2>Reset your password</h2>" +
                "<p>Click the button below to reset your password.</p>" +
                "<p><a href='" + link + "' style='display:inline-block;padding:10px 16px;background:#2563eb;color:#fff;border-radius:8px;text-decoration:none'>Reset password</a></p>" +
                "</div>";
    }
}


