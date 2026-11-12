package com.seaandtea.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentIntentResponse {
    private Long bookingId;
    private String paymentIntentId;
    private String clientSecret;
    private String publishableKey;
    private String status;
}
