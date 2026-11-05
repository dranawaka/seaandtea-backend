package com.seaandtea.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "tours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Tour {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guide_id", nullable = false)
    private Guide guide;
    
    @Column(nullable = false)
    private String title;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TourCategory category;
    
    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;
    
    @Column(name = "max_group_size")
    private Integer maxGroupSize = 10;
    
    @Column(name = "price_per_person", nullable = false)
    private BigDecimal pricePerPerson;
    
    @Column(name = "instant_booking")
    private Boolean instantBooking = false;
    
    @Column(name = "secure_payment")
    private Boolean securePayment = true;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "languages")
    private List<String> languages;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "highlights")
    private List<String> highlights;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "included_items")
    private List<String> includedItems;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "excluded_items")
    private List<String> excludedItems;
    
    @Column(name = "meeting_point")
    private String meetingPoint;
    
    @Column(name = "cancellation_policy")
    private String cancellationPolicy;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "tour", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TourImage> images;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TourCategory {
        TEA_TOURS, BEACH_TOURS, CULTURAL_TOURS, ADVENTURE_TOURS, FOOD_TOURS, WILDLIFE_TOURS
    }
}

