package com.cadt.devices.service.common;

public interface EmailService {
    void sendEmail(String to, String subject, String htmlBody);
}


