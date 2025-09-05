package com.cadt.devices.dto.catalog;

import com.cadt.devices.model.media.MediaType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MediaDto {
    private String id;
    private String url;
    private MediaType type;
    private String alt;
    private int sortOrder;
}
