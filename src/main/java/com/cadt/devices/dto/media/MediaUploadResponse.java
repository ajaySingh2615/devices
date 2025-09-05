package com.cadt.devices.dto.media;

import com.cadt.devices.model.media.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaUploadResponse {
    private String id;
    private String url;
    private String publicId;
    private MediaType type;
    private String alt;
    private Integer sortOrder;
}
