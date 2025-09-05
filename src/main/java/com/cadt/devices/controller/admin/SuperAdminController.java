package com.cadt.devices.controller.admin;

import com.cadt.devices.dto.auth.RegisterRequest;
import com.cadt.devices.dto.auth.AuthResponse;
import com.cadt.devices.service.auth.AuthService;
import com.cadt.devices.model.user.Role;
import com.cadt.devices.repo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;

@RestController
@RequestMapping(value = "/api/v1/super-admin", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SuperAdminController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping(value = "/initialize", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> initializeSuperAdmin(@RequestBody RegisterRequest request) {
        try {
            // Check if any admin already exists
            boolean adminExists = userRepository.existsByRole(Role.ADMIN);
            
            if (adminExists) {
                return ResponseEntity.badRequest()
                    .body("Super admin already exists. This endpoint can only be used once.");
            }

            // Validate the secret key for additional security
            String secretKey = request.getSecretKey();
            if (!"DEVICE_HUB_SUPER_ADMIN_2024".equals(secretKey)) {
                return ResponseEntity.badRequest()
                    .body("Invalid secret key. Contact system administrator.");
            }

            // Register the super admin
            AuthResponse response = authService.registerSuperAdmin(request);
            
            return ResponseEntity.ok()
                .body(new SuperAdminInitResponse(
                    "Super admin created successfully", 
                    response.getUser().getName(),
                    response.getUser().getEmail()
                ));
                
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body("Failed to create super admin: " + e.getMessage());
        }
    }

    @GetMapping(value = "/check", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> checkAdminStatus() {
        boolean adminExists = userRepository.existsByRole(Role.ADMIN);
        long adminCount = userRepository.countByRole(Role.ADMIN);
        
        return ResponseEntity.ok()
            .body(new AdminStatusResponse(adminExists, adminCount));
    }

    // Response DTOs
    public static class SuperAdminInitResponse {
        private String message;
        private String adminName;
        private String adminEmail;
        private String instructions;

        public SuperAdminInitResponse(String message, String adminName, String adminEmail) {
            this.message = message;
            this.adminName = adminName;
            this.adminEmail = adminEmail;
            this.instructions = "You can now login with these credentials and access the admin panel at /admin";
        }

        // Getters
        public String getMessage() { return message; }
        public String getAdminName() { return adminName; }
        public String getAdminEmail() { return adminEmail; }
        public String getInstructions() { return instructions; }
    }

    public static class AdminStatusResponse {
        @JsonProperty("adminExists")
        private boolean adminExists;
        
        @JsonProperty("adminCount")
        private long adminCount;
        
        @JsonProperty("status")
        private String status;

        public AdminStatusResponse(boolean adminExists, long adminCount) {
            this.adminExists = adminExists;
            this.adminCount = adminCount;
            this.status = adminExists ? 
                "Admin users exist. Super admin initialization not available." : 
                "No admin users found. Super admin initialization available.";
        }

        // Getters
        public boolean isAdminExists() { return adminExists; }
        public long getAdminCount() { return adminCount; }
        public String getStatus() { return status; }
    }
}
