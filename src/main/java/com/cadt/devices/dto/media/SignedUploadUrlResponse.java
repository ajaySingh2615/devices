package com.cadt.devices.dto.media;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SignedUploadUrlResponse {
    private String uploadUrl;
    private Map<String, Object> uploadParameters;
    private Long expiresAt;
}
