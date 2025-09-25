package com.cadt.devices.controller.webhook;

import com.cadt.devices.model.notification.EmailSuppression;
import com.cadt.devices.repo.notification.EmailSuppressionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks/resend")
public class ResendWebhookController {
    private static final Logger log = LoggerFactory.getLogger(ResendWebhookController.class);
    private final EmailSuppressionRepository suppressions;

    public ResendWebhookController(EmailSuppressionRepository suppressions) {
        this.suppressions = suppressions;
    }

    @PostMapping
    public ResponseEntity<Void> handle(@RequestBody Map<String, Object> payload) {
        // Minimal stub: log event and TODO suppressions
        try {
            log.info("Resend webhook: {}", payload);
            String type = String.valueOf(payload.get("type"));
            Map<String, Object> data = (Map<String, Object>) payload.get("data");
            if (data != null) {
                String to = (String) data.get("to");
                if (to == null && data.get("recipient") instanceof String) {
                    to = (String) data.get("recipient");
                }
                if (to != null && ("email.bounced".equals(type) || "email.complained".equals(type))) {
                    if (!suppressions.existsByEmail(to)) {
                        suppressions.save(EmailSuppression.builder()
                                .id(java.util.UUID.randomUUID().toString())
                                .email(to)
                                .reason("email.bounced".equals(type) ? "bounce" : "complaint")
                                .provider("resend")
                                .payload(payload.toString())
                                .createdAt(java.time.Instant.now())
                                .build());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Resend webhook error", e);
        }
        return ResponseEntity.ok().build();
    }
}


