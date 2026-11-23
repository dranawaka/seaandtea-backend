package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatisticsResponse {
    private long totalUsers;
    private long verifiedGuides;
    private long pendingGuides;
    private long totalBookings;
}
