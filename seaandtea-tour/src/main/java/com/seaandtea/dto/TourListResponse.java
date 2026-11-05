package com.seaandtea.dto;

import com.seaandtea.entity.Tour.TourCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourListResponse {
    
    private Long id;
    private String title;
    private String description;
    private TourCategory category;
    private Integer durationHours;
    private Integer maxGroupSize;
    private BigDecimal pricePerPerson;
    private Boolean instantBooking;
    private String primaryImageUrl;
    private LocalDateTime createdAt;
    
    // Guide basic info
    private String guideName;
    private String guideProfilePicture;
    private BigDecimal guideAverageRating;
    private Boolean guideIsVerified;
    
    // Tour statistics
    private BigDecimal averageRating;
    private Long totalReviews;
    private Long totalBookings;
    
    // Highlights preview (first 3)
    private String highlightsPreview;
}


