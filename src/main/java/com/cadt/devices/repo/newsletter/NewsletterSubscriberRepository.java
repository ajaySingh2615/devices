package com.cadt.devices.repo.newsletter;

import com.cadt.devices.model.newsletter.NewsletterSubscriber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriberRepository extends JpaRepository<NewsletterSubscriber, String> {
    boolean existsByEmailIgnoreCase(String email);
}


