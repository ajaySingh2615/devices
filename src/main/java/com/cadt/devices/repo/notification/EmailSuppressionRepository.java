package com.cadt.devices.repo.notification;

import com.cadt.devices.model.notification.EmailSuppression;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailSuppressionRepository extends JpaRepository<EmailSuppression, String> {
    Optional<EmailSuppression> findByEmail(String email);
    boolean existsByEmail(String email);
}


