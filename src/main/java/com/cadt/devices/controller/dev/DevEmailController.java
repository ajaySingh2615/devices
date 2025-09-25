package com.cadt.devices.controller.dev;

import com.cadt.devices.service.common.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dev")
public class DevEmailController {
    private static final Logger log = LoggerFactory.getLogger(DevEmailController.class);
    private final EmailService emailService;

    public DevEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/test-email")
    public ResponseEntity<?> test(@RequestParam("to") String to) {
        try {
            log.info("[dev] sending test email to={} using EmailService", to);
            emailService.sendEmail(to, "Test Email", "<p>This is a test email from DeviceHub backend.</p>");
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (Exception e) {
            log.error("[dev] test email failed", e);
            return ResponseEntity.status(500).body(Map.of("ok", false, "error", e.getMessage()));
        }
    }
}


