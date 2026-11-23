package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideDashboardResponse {

    private GuideProfileResponse profile;
    private Integer totalTours;
    private Integer activeTours;
    private Integer totalBookings;
    private Integer thisMonthBookings;
    private BigDecimal totalRevenue;
    private BigDecimal averageRating;
    private Integer totalReviews;
}
