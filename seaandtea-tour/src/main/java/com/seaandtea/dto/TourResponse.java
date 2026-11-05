package com.seaandtea.dto;

import com.seaandtea.entity.Tour.TourCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourResponse {
    
    private Long id;
    private String title;
    private String description;
    private TourCategory category;
    private Integer durationHours;
    private Integer maxGroupSize;
    private BigDecimal pricePerPerson;
    private Boolean instantBooking;
    private Boolean securePayment;
    private List<String> languages;
    private List<String> highlights;
    private List<String> includedItems;
    private List<String> excludedItems;
    private String meetingPoint;
    private String cancellationPolicy;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Guide information
    private GuideBasicInfo guide;
    
    // Images
    private List<TourImageDto> images;
    
    // Statistics
    private Long totalBookings;
    private BigDecimal averageRating;
    private Long totalReviews;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideBasicInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String profilePictureUrl;
        private BigDecimal averageRating;
        private Integer totalTours;
        private Boolean isVerified;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourImageDto {
        private Long id;
        private String imageUrl;
        private Boolean isPrimary;
        private String altText;
        private LocalDateTime createdAt;
    }
}


