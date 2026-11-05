package com.seaandtea.dto;

import com.seaandtea.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Admin view of a user, optionally including guide summary.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private LocalDate dateOfBirth;
    private String nationality;
    private String profilePictureUrl;
    private Boolean isVerified;
    private Boolean isActive;
    private User.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Present when user has a guide profile. */
    private GuideSummary guideSummary;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GuideSummary {
        private Long guideId;
        private String verificationStatus;
        private Boolean isAvailable;
        private Integer totalTours;
        private BigDecimal averageRating;
        private Integer totalReviews;
    }
}
