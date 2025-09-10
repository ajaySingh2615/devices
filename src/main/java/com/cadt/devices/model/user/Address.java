package com.cadt.devices.model.user;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Entity
@Table(name = "addresses", indexes = {
        @Index(name = "idx_addresses_user", columnList = "user_id"),
        @Index(name = "idx_addresses_default", columnList = "is_default")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(length = 120, nullable = false)
    private String name;

    @Column(length = 20)
    private String phone;

    @NotBlank
    @Column(name = "line1", length = 255, nullable = false)
    private String line1;

    @Column(name = "line2", length = 255)
    private String line2;

    @NotBlank
    @Column(length = 120, nullable = false)
    private String city;

    @NotBlank
    @Column(length = 120, nullable = false)
    private String state;

    @NotBlank
    @Column(length = 120, nullable = false)
    private String country;

    @NotBlank
    @Pattern(regexp = "^\\d{6}$")
    @Column(length = 10, nullable = false)
    private String pincode;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;
}


