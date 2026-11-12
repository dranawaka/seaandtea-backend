package com.seaandtea.controller;

import com.seaandtea.dto.PaymentConfirmRequest;
import com.seaandtea.dto.PaymentIntentResponse;
import com.seaandtea.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> config() {
        return ResponseEntity.ok(Map.of("publishableKey", paymentService.publishableKey() == null ? "" : paymentService.publishableKey()));
    }

    @PostMapping("/intent")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @RequestParam Long bookingId,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.createIntent(bookingId, authentication.getName()));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<PaymentIntentResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(paymentService.confirm(request, authentication.getName()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(HttpServletRequest request) throws Exception {
        String payload = new String(request.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String signature = request.getHeader("Stripe-Signature");
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok("ok");
    }
}
