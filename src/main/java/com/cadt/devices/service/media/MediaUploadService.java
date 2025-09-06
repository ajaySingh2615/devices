package com.cadt.devices.service.media;

import com.cadt.devices.dto.media.MediaUploadRequest;
import com.cadt.devices.dto.media.MediaUploadResponse;
import com.cadt.devices.dto.media.SignedUploadUrlRequest;
import com.cadt.devices.dto.media.SignedUploadUrlResponse;
import com.cadt.devices.model.media.Media;
import com.cadt.devices.model.media.MediaOwnerType;
import com.cadt.devices.model.media.MediaType;
import com.cadt.devices.repo.media.MediaRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaUploadService {

    private final MediaRepository mediaRepository;
    private final Cloudinary cloudinary;

    @Value("${cloudinary.upload-preset:device_hub_uploads}")
    private String uploadPreset;

    /**
     * Generate signed upload URL for direct client uploads
     */
    public SignedUploadUrlResponse generateSignedUploadUrl(SignedUploadUrlRequest request) {
        try {
            // Create upload parameters for signature generation (exclude resource_type)
            @SuppressWarnings("unchecked")
            Map<String, Object> signatureParams = (Map<String, Object>) ObjectUtils.asMap(
                "folder", "device_hub/" + request.getOwnerType().name().toLowerCase(),
                "public_id_prefix", request.getOwnerType().name().toLowerCase() + "_" + request.getOwnerId() + "_",
                "timestamp", System.currentTimeMillis() / 1000
            );
            
            // Create full params for the actual upload (include resource_type)
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) ObjectUtils.asMap(
                "folder", "device_hub/" + request.getOwnerType().name().toLowerCase(),
                "resource_type", getCloudinaryResourceType(request.getMediaType()),
                "public_id_prefix", request.getOwnerType().name().toLowerCase() + "_" + request.getOwnerId() + "_",
                "timestamp", System.currentTimeMillis() / 1000
            );

            // Note: Transformations should be handled by upload preset or applied via URL
            // Removing transformation from signed upload to avoid FormData serialization issues

            // Debug logging
            log.info("Signature params (excluding resource_type): {}", signatureParams);
            log.info("Full params (including resource_type): {}", params);
            log.info("API Secret: {}", cloudinary.config.apiSecret.substring(0, 5) + "...");
            
            // Generate signature using only signature-specific params (excluding resource_type, api_key, signature)
            String signature = cloudinary.apiSignRequest(signatureParams, cloudinary.config.apiSecret);
            
            log.info("Generated signature: {}", signature);
            
            // Add signature and api_key to the final parameters
            params.put("signature", signature);
            params.put("api_key", cloudinary.config.apiKey);
            
            log.info("Final params: {}", params);

            String uploadUrl = "https://api.cloudinary.com/v1_1/" + cloudinary.config.cloudName + "/" 
                             + getCloudinaryResourceType(request.getMediaType()) + "/upload";

            return SignedUploadUrlResponse.builder()
                .uploadUrl(uploadUrl)
                .uploadParameters(params)
                .expiresAt(System.currentTimeMillis() + (60 * 60 * 1000)) // 1 hour
                .build();

        } catch (Exception e) {
            log.error("Failed to generate signed upload URL", e);
            throw new RuntimeException("Failed to generate upload URL: " + e.getMessage());
        }
    }

    /**
     * Upload file directly to Cloudinary (for server-side uploads)
     */
    @Transactional
    public MediaUploadResponse uploadFile(MultipartFile file, MediaUploadRequest request) {
        try {
            // Upload to Cloudinary
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadParams = (Map<String, Object>) ObjectUtils.asMap(
                "folder", "device_hub/" + request.getOwnerType().name().toLowerCase(),
                "public_id_prefix", request.getOwnerType().name().toLowerCase() + "_" + request.getOwnerId() + "_",
                "resource_type", getCloudinaryResourceType(request.getMediaType())
            );

            // Add image transformations
            if (request.getMediaType() == MediaType.IMAGE) {
                uploadParams.put("transformation", ObjectUtils.asMap(
                    "quality", "auto:good",
                    "fetch_format", "auto"
                ));
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(file.getBytes(), uploadParams);
            
            // Save media record to database
            Media media = Media.builder()
                .ownerType(request.getOwnerType())
                .ownerId(request.getOwnerId())
                .url((String) uploadResult.get("secure_url"))
                .type(request.getMediaType())
                .alt(request.getAlt())
                .sortOrder(request.getSortOrder())
                .build();

            media = mediaRepository.save(media);

            return MediaUploadResponse.builder()
                .id(media.getId())
                .url(media.getUrl())
                .publicId((String) uploadResult.get("public_id"))
                .type(media.getType())
                .alt(media.getAlt())
                .sortOrder(media.getSortOrder())
                .build();

        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary", e);
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }

    /**
     * Save media metadata after successful client upload
     */
    @Transactional
    public MediaUploadResponse saveMediaMetadata(MediaUploadRequest request, String cloudinaryUrl, String publicId) {
        Media media = Media.builder()
            .ownerType(request.getOwnerType())
            .ownerId(request.getOwnerId())
            .url(cloudinaryUrl)
            .type(request.getMediaType())
            .alt(request.getAlt())
            .sortOrder(request.getSortOrder())
            .build();

        media = mediaRepository.save(media);

        return MediaUploadResponse.builder()
            .id(media.getId())
            .url(media.getUrl())
            .publicId(publicId)
            .type(media.getType())
            .alt(media.getAlt())
            .sortOrder(media.getSortOrder())
            .build();
    }

    /**
     * Get media by owner
     */
    public List<MediaUploadResponse> getMediaByOwner(MediaOwnerType ownerType, String ownerId) {
        List<Media> mediaList = mediaRepository.findByOwnerTypeAndOwnerIdOrderBySortOrder(ownerType, ownerId);
        
        return mediaList.stream()
            .map(media -> MediaUploadResponse.builder()
                .id(media.getId())
                .url(media.getUrl())
                .type(media.getType())
                .alt(media.getAlt())
                .sortOrder(media.getSortOrder())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Delete media
     */
    @Transactional
    public void deleteMedia(String mediaId) {
        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new RuntimeException("Media not found"));

        // Delete from Cloudinary (extract public_id from URL)
        try {
            String publicId = extractPublicIdFromUrl(media.getUrl());
            if (publicId != null) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            log.warn("Failed to delete from Cloudinary: " + e.getMessage());
        }

        // Delete from database
        mediaRepository.delete(media);
    }

    /**
     * Update media order
     */
    @Transactional
    public void updateMediaOrder(String mediaId, int sortOrder) {
        Media media = mediaRepository.findById(mediaId)
            .orElseThrow(() -> new RuntimeException("Media not found"));
        
        media.setSortOrder(sortOrder);
        mediaRepository.save(media);
    }

    private String getCloudinaryResourceType(MediaType mediaType) {
        return switch (mediaType) {
            case IMAGE -> "image";
            case VIDEO -> "video";
            case DOCUMENT -> "raw";
        };
    }

    private String extractPublicIdFromUrl(String url) {
        try {
            // Extract public_id from Cloudinary URL
            // URL format: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/public_id.ext
            String[] parts = url.split("/");
            if (parts.length >= 7) {
                String fileName = parts[parts.length - 1];
                String publicId = fileName.substring(0, fileName.lastIndexOf('.'));
                String folder = parts[parts.length - 2];
                return folder + "/" + publicId;
            }
        } catch (Exception e) {
            log.warn("Failed to extract public_id from URL: " + url);
        }
        return null;
    }
}
