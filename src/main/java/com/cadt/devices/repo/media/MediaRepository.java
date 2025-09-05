package com.cadt.devices.repo.media;

import com.cadt.devices.model.media.Media;
import com.cadt.devices.model.media.MediaOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MediaRepository extends JpaRepository<Media, String> {

    List<Media> findByOwnerTypeAndOwnerIdOrderBySortOrder(MediaOwnerType ownerType, String ownerId);

    void deleteByOwnerTypeAndOwnerId(MediaOwnerType ownerType, String ownerId);
}
