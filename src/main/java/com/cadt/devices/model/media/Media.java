package com.cadt.devices.model.media;

import com.cadt.devices.model.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "media", indexes = {
        @Index(name = "idx_media_owner", columnList = "ownerType,ownerId"),
        @Index(name = "idx_media_sort", columnList = "ownerId,sortOrder")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Media extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaOwnerType ownerType;

    @Column(nullable = false)
    private String ownerId;

    @NotBlank
    @Column(length = 500, nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MediaType type;

    @Column(length = 200)
    private String alt;

    @Column(nullable = false)
    @Builder.Default
    private int sortOrder = 0;
}
