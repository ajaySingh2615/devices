package com.cadt.devices.dto.admin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class RecentActivityData {
    private String id;
    private String type;
    private String description;
    private Instant timestamp;
    private String user;
}
