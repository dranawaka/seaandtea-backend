package com.seaandtea.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class ThingBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ThingStatus status = ThingStatus.DRAFT;

    @Column(length = 300)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "hero_image_url", length = 1000)
    private String heroImageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_id")
    private Destination destination;

    private Double latitude;

    private Double longitude;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "verified_by_user_id")
    private Long verifiedByUserId;

    @Column(name = "submitted_by_user_id")
    private Long submittedByUserId;

    @Column(name = "needs_verification", columnDefinition = "TEXT")
    private String needsVerification;

    @Column(name = "moderation_note", columnDefinition = "TEXT")
    private String moderationNote;

    @Column(name = "seo_title", length = 120)
    private String seoTitle;

    @Column(name = "seo_description", length = 300)
    private String seoDescription;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Seed writes set this so {@link #onUpdate()} does not make a seed refresh look like an admin edit.
     */
    @Transient
    private boolean preserveTimestamps;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = ThingStatus.DRAFT;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (!preserveTimestamps) {
            updatedAt = LocalDateTime.now();
        }
    }

    public boolean wasEditedAfterCreate() {
        return updatedAt != null && createdAt != null && updatedAt.isAfter(createdAt);
    }
}
