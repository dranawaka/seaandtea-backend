package com.seaandtea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentConfirmRequest {

    @NotNull(message = "Booking is required")
    private Long bookingId;

    @NotBlank(message = "Payment intent is required")
    private String paymentIntentId;
}
