package com.cadt.devices.service.common;

import com.cadt.devices.repo.notification.EmailSuppressionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class ResendEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private final String apiKey;
    private final String from;
    private final RestTemplate http = new RestTemplate();
    private final EmailSuppressionRepository suppressions;

    public ResendEmailService(
            @Value("${resend.apiKey}") String apiKey,
            @Value("${resend.from}") String from,
            EmailSuppressionRepository suppressions
    ) {
        this.apiKey = apiKey;
        this.from = from;
        this.suppressions = suppressions;
    }

    @Override
    public void sendEmail(String to, String subject, String htmlBody) {
        if (to == null || to.isBlank()) return;
        if (suppressions.existsByEmail(to)) return; // do not send to suppressed
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("from", from);
        body.put("to", new String[]{to});
        body.put("subject", subject);
        body.put("html", htmlBody);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            var resp = http.postForEntity("https://api.resend.com/emails", entity, String.class);
            log.info("Resend sendEmail to={} status={} body={}", to, resp.getStatusCodeValue(), resp.getBody());
        } catch (Exception e) {
            log.error("Resend sendEmail failed to={} subject={}", to, subject, e);
        }
    }
}


