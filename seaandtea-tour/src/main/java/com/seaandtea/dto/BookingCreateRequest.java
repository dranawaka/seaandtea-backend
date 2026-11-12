package com.seaandtea.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingCreateRequest {

    @NotNull(message = "Tour is required")
    private Long tourId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Number of people is required")
    @Min(value = 1, message = "At least one person is required")
    private Integer numberOfPeople;

    @Size(max = 1000, message = "Special requests cannot exceed 1000 characters")
    private String specialRequests;
}
