package com.cadt.devices.controller.checkout;

import com.cadt.devices.dto.checkout.CheckoutSummaryRequest;
import com.cadt.devices.dto.checkout.CheckoutSummaryResponse;
import com.cadt.devices.service.checkout.CheckoutService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkout;

    @PostMapping("/summary")
    public ResponseEntity<CheckoutSummaryResponse> summary(
            Authentication auth,
            HttpServletRequest req,
            @Valid @RequestBody CheckoutSummaryRequest r,
            @RequestParam(required = false) String sessionId
    ) {
        String userId = auth != null ? auth.getName() : null;
        String session = sessionId != null ? sessionId : req.getSession(true).getId();
        return ResponseEntity.ok(checkout.summarize(userId, session, r));
    }
}


