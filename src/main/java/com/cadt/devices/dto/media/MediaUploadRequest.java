package com.cadt.devices.dto.media;

import com.cadt.devices.model.media.MediaOwnerType;
import com.cadt.devices.model.media.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MediaUploadRequest {
    
    @NotNull(message = "Owner type is required")
    private MediaOwnerType ownerType;
    
    @NotBlank(message = "Owner ID is required")
    private String ownerId;
    
    @NotNull(message = "Media type is required")
    private MediaType mediaType;
    
    private String alt;
    
    private Integer sortOrder = 0;
}
