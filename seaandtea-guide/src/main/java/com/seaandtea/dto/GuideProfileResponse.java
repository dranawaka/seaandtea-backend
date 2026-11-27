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

    @JsonView(ApiViews.Public.class)
    private Long id;
    @JsonView(ApiViews.Public.class)
    private Long userId;
    @JsonView(ApiViews.Public.class)
    private String userFirstName;
    @JsonView(ApiViews.Public.class)
    private String userLastName;
    @JsonView(ApiViews.Internal.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userEmail;
    @JsonView(ApiViews.Public.class)
    private String profilePictureUrl;
    @JsonView(ApiViews.Public.class)
    private String bio;
    @JsonView(ApiViews.Public.class)
    private BigDecimal hourlyRate;
    @JsonView(ApiViews.Public.class)
    private BigDecimal dailyRate;
    @JsonView(ApiViews.Public.class)
    private Integer responseTimeHours;
    @JsonView(ApiViews.Public.class)
    private Boolean isAvailable;
    @JsonView(ApiViews.Public.class)
    private Integer totalTours;
    @JsonView(ApiViews.Public.class)
    private BigDecimal averageRating;
    @JsonView(ApiViews.Public.class)
    private Integer totalReviews;
    @JsonView(ApiViews.Public.class)
    private String verificationStatus;
    @JsonView(ApiViews.Internal.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> verificationDocuments;
    @JsonView(ApiViews.Public.class)
    private LocalDateTime createdAt;
    @JsonView(ApiViews.Public.class)
    private LocalDateTime updatedAt;
    @JsonView(ApiViews.Public.class)
    private List<GuideSpecialtyResponse> specialties;
    @JsonView(ApiViews.Public.class)
    private List<GuideLanguageResponse> languages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideSpecialtyResponse {
        @JsonView(ApiViews.Public.class)
        private Long id;
        @JsonView(ApiViews.Public.class)
        private String specialty;
        @JsonView(ApiViews.Public.class)
        private Integer yearsExperience;
        @JsonView(ApiViews.Internal.class)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String certificationUrl;
        @JsonView(ApiViews.Public.class)
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideLanguageResponse {
        @JsonView(ApiViews.Public.class)
        private Long id;
        @JsonView(ApiViews.Public.class)
        private String language;
        @JsonView(ApiViews.Public.class)
        private String proficiencyLevel;
        @JsonView(ApiViews.Public.class)
        private LocalDateTime createdAt;
    }
}

