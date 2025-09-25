package com.cadt.devices.model.user;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.Instant;


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

    @Column(name = "first_name", length = 80)
    private String firstName;

    @Column(name = "last_name", length = 80)
    private String lastName;

    public enum Gender { MALE, FEMALE, UNSPECIFIED }

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 16)
    private Gender gender;

    @Column(name = "email_verified_at")
    private Instant emailVerifiedAt;

    @Column(name = "phone_verified_at")
    private Instant phoneVerifiedAt;

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
