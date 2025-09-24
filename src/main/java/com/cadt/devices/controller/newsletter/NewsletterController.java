package com.cadt.devices.controller.newsletter;

import com.cadt.devices.dto.newsletter.NewsletterSubscribeRequest;
import com.cadt.devices.service.newsletter.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/newsletter")
@RequiredArgsConstructor
public class NewsletterController {
    private final NewsletterService service;

    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody NewsletterSubscribeRequest req) {
        service.subscribe(req);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}


