package com.cadt.devices.service.user;

import com.cadt.devices.dto.user.UpdateProfileRequest;
import com.cadt.devices.dto.user.UserResponse;
import com.cadt.devices.model.user.User;
import com.cadt.devices.model.user.Role;
import com.cadt.devices.model.user.UserStatus;
import com.cadt.devices.repo.user.UserRepository;
import com.cadt.devices.util.PhoneUtil;
import com.cadt.devices.exception.ApiException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(String id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional
    public User update(String id, UpdateProfileRequest r) {
        var u = get(id);
        u.setName(r.getName());
        // Normalize phone number for consistency
        u.setPhone(PhoneUtil.normalizePhone(r.getPhone()));
        u.setAvatarUrl(r.getAvatarUrl());
        if (r.getFirstName() != null) u.setFirstName(r.getFirstName());
        if (r.getLastName() != null) u.setLastName(r.getLastName());
        if (r.getGender() != null) {
            try {
                u.setGender(User.Gender.valueOf(r.getGender().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        return userRepository.save(u);
    }

    // Admin user management methods
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse updateUserRole(String userId, String role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found"));
        
        try {
            user.setRole(Role.valueOf(role.toUpperCase()));
            User savedUser = userRepository.save(user);
            return toUserResponse(savedUser);
        } catch (IllegalArgumentException e) {
            throw new ApiException("INVALID_ROLE", "Invalid role: " + role);
        }
    }

    @Transactional
    public UserResponse updateUserStatus(String userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("USER_NOT_FOUND", "User not found"));
        
        try {
            user.setStatus(UserStatus.valueOf(status.toUpperCase()));
            User savedUser = userRepository.save(user);
            return toUserResponse(savedUser);
        } catch (IllegalArgumentException e) {
            throw new ApiException("INVALID_STATUS", "Invalid status: " + status);
        }
    }

    @Transactional
    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("USER_NOT_FOUND", "User not found");
        }
        userRepository.deleteById(userId);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.getAvatarUrl(),
                user.getStatus().name(),
                user.getGender() != null ? user.getGender().name() : null,
                user.getCreatedAt(),
                user.getEmailVerifiedAt(),
                user.getPhoneVerifiedAt()
        );
    }
}
