package com.seaandtea.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
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
public class GuideProfileResponse {
    
    private Long id;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    @JsonView(ApiViews.Internal.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userEmail;
    private String profilePictureUrl;
    private String bio;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private Integer responseTimeHours;
    private Boolean isAvailable;
    private Integer totalTours;
    private BigDecimal averageRating;
    private Integer totalReviews;
    private String verificationStatus;
    @JsonView(ApiViews.Internal.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> verificationDocuments;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<GuideSpecialtyResponse> specialties;
    private List<GuideLanguageResponse> languages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideSpecialtyResponse {
        private Long id;
        private String specialty;
        private Integer yearsExperience;
        @JsonView(ApiViews.Internal.class)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String certificationUrl;
        private LocalDateTime createdAt;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideLanguageResponse {
        private Long id;
        private String language;
        private String proficiencyLevel;
        private LocalDateTime createdAt;
    }
}

