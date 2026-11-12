package com.seaandtea.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class AvailabilityDayResponse {
    private LocalDate date;
    private Integer remainingSpots;
    private LocalTime departureTime;
}
