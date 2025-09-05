package com.cadt.devices.controller.media;

import com.cadt.devices.dto.media.*;
import com.cadt.devices.model.media.MediaOwnerType;
import com.cadt.devices.service.media.MediaUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/media")
@RequiredArgsConstructor
public class MediaUploadController {

    private final MediaUploadService mediaUploadService;

    /**
     * Generate signed upload URL for direct client uploads
     */
    @PostMapping("/upload-url")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<SignedUploadUrlResponse> generateUploadUrl(@Valid @RequestBody SignedUploadUrlRequest request) {
        SignedUploadUrlResponse response = mediaUploadService.generateSignedUploadUrl(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Direct file upload (server-side)
     */
    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @Valid @ModelAttribute MediaUploadRequest request) {
        MediaUploadResponse response = mediaUploadService.uploadFile(file, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Save media metadata after successful client upload
     */
    @PostMapping("/metadata")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<MediaUploadResponse> saveMediaMetadata(
            @Valid @RequestBody MediaUploadRequest request,
            @RequestParam String cloudinaryUrl,
            @RequestParam String publicId) {
        MediaUploadResponse response = mediaUploadService.saveMediaMetadata(request, cloudinaryUrl, publicId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get media by owner
     */
    @GetMapping("/owner/{ownerType}/{ownerId}")
    public ResponseEntity<List<MediaUploadResponse>> getMediaByOwner(
            @PathVariable MediaOwnerType ownerType,
            @PathVariable String ownerId) {
        List<MediaUploadResponse> media = mediaUploadService.getMediaByOwner(ownerType, ownerId);
        return ResponseEntity.ok(media);
    }

    /**
     * Delete media
     */
    @DeleteMapping("/{mediaId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        mediaUploadService.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update media order
     */
    @PutMapping("/{mediaId}/order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateMediaOrder(
            @PathVariable String mediaId,
            @RequestParam int sortOrder) {
        mediaUploadService.updateMediaOrder(mediaId, sortOrder);
        return ResponseEntity.ok().build();
    }
}
