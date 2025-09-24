package com.cadt.devices.dto.newsletter;

import lombok.Data;

@Data
public class NewsletterSubscribeRequest {
    private String email;
    private String source;
}


