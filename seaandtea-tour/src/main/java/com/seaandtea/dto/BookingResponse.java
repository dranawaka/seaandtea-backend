package com.seaandtea.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class BookingResponse {
    private Long id;
    private Long tourId;
    private String tourTitle;
    private String tourImage;
    private String destination;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer numberOfPeople;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private String paymentStatus;
    private String specialRequests;
    private LocalDateTime createdAt;
}
