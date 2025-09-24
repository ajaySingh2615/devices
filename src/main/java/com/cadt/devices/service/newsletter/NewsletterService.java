package com.cadt.devices.service.newsletter;

import com.cadt.devices.dto.newsletter.NewsletterSubscribeRequest;
import com.cadt.devices.exception.ApiException;
import com.cadt.devices.model.newsletter.NewsletterSubscriber;
import com.cadt.devices.repo.newsletter.NewsletterSubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NewsletterService {
    private final NewsletterSubscriberRepository repo;

    @Transactional
    public void subscribe(NewsletterSubscribeRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new ApiException("INVALID_EMAIL", "Valid email is required");
        }
        String email = req.getEmail().trim();
        if (repo.existsByEmailIgnoreCase(email)) {
            // Idempotent: treat as success
            return;
        }
        NewsletterSubscriber sub = NewsletterSubscriber.builder()
                .email(email)
                .source(req.getSource())
                .build();
        repo.save(sub);
    }
}


