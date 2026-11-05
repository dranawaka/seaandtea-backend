package com.seaandtea.dto;

import com.seaandtea.entity.Tour.TourCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourFilterRequest {
    
    private String searchTerm;
    private TourCategory category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer minDuration;
    private Integer maxDuration;
    private Boolean instantBooking;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "createdAt";
    private String sortDirection = "desc";
}


