package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuideProfileRequest {
    
    @NotBlank(message = "Bio is required")
    @Size(min = 50, message = "Bio must be at least 50 characters long")
    private String bio;
    
    @NotNull(message = "Hourly rate is required")
    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    @Digits(integer = 5, fraction = 2, message = "Hourly rate must have up to 5 digits and 2 decimal places")
    private BigDecimal hourlyRate;
    
    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.01", message = "Daily rate must be greater than 0")
    @Digits(integer = 5, fraction = 2, message = "Daily rate must have up to 5 digits and 2 decimal places")
    private BigDecimal dailyRate;
    
    @NotNull(message = "Response time is required")
    @Min(value = 1, message = "Response time must be at least 1 hour")
    @Max(value = 168, message = "Response time cannot exceed 168 hours (1 week)")
    private Integer responseTimeHours;
    
    @NotNull(message = "Availability status is required")
    private Boolean isAvailable;
    
    @NotEmpty(message = "At least one specialty is required")
    @Size(max = 10, message = "Maximum 10 specialties allowed")
    private List<GuideSpecialtyRequest> specialties;
    
    @NotEmpty(message = "At least one language is required")
    @Size(max = 5, message = "Maximum 5 languages allowed")
    private List<GuideLanguageRequest> languages;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideSpecialtyRequest {
        @NotBlank(message = "Specialty name is required")
        private String specialty;
        
        @Min(value = 0, message = "Years of experience cannot be negative")
        @Max(value = 50, message = "Years of experience cannot exceed 50")
        private Integer yearsExperience;
        
        private String certificationUrl;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideLanguageRequest {
        @NotBlank(message = "Language is required")
        private String language;
        
        @NotNull(message = "Proficiency level is required")
        private String proficiencyLevel;
    }
}
