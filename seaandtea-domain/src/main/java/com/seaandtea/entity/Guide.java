package com.seaandtea.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "guides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Guide {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    private String bio;
    
    @Column(name = "hourly_rate")
    private BigDecimal hourlyRate;
    
    @Column(name = "daily_rate")
    private BigDecimal dailyRate;
    
    @Column(name = "response_time_hours")
    private Integer responseTimeHours = 24;
    
    @Column(name = "is_available")
    private Boolean isAvailable = true;
    
    @Column(name = "total_tours")
    private Integer totalTours = 0;
    
    @Column(name = "average_rating")
    private BigDecimal averageRating = BigDecimal.ZERO;
    
    @Column(name = "total_reviews")
    private Integer totalReviews = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;
    
    @Column(name = "verification_documents")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> verificationDocuments;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuideSpecialty> specialties;
    
    @OneToMany(mappedBy = "guide", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GuideLanguage> languages;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum VerificationStatus {
        PENDING, VERIFIED, REJECTED
    }
}

