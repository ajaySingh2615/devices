package com.cadt.devices.model.user;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


@Entity
@Table(name = "users", indexes = {@Index(name = "uk_users_email",
        columnList = "email", unique = true),
        @Index(name = "uk_users_phone", columnList = "phone", unique = true)})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class User extends BaseEntity {
    @Email
    @Column(length = 191, unique = true)
    private String email;

    @Column(length = 20, unique = true)
    private String phone;

    private String passwordHash;

    @Column(length = 64, unique = true)
    private String googleSub;

    @NotBlank
    @Column(length = 120, nullable = false)
    private String name;

    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.CUSTOMER;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
}
