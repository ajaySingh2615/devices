package com.cadt.devices.repo.user;

import com.cadt.devices.model.user.User;
import com.cadt.devices.model.user.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);
    
    Optional<User> findByGoogleSub(String googleSub);

    boolean existsByEmail(String email);
    
    // Admin dashboard queries
    long countByCreatedAtAfter(LocalDateTime date);
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Role-based queries
    boolean existsByRole(Role role);
    long countByRole(Role role);
}
