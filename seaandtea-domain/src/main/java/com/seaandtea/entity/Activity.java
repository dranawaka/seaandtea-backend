package com.seaandtea.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "activities", indexes = {
        @Index(name = "idx_activities_status_destination", columnList = "status, destination_id"),
        @Index(name = "idx_activities_status_category", columnList = "status, category")
})
@Getter
@Setter
public class Activity extends ThingBase {

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ActivityCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ActivityRegion region;

    @Column(name = "year_round", nullable = false)
    private boolean yearRound;

    @Column(name = "season_note", columnDefinition = "TEXT")
    private String seasonNote;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Difficulty difficulty;

    @Column(name = "typical_duration_minutes")
    private Integer typicalDurationMinutes;

    @Column(name = "best_time_of_day", length = 200)
    private String bestTimeOfDay;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "activity_tour_categories", joinColumns = @JoinColumn(name = "activity_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "tour_category", nullable = false, length = 40)
    private Set<Tour.TourCategory> tourCategories = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "activity_nearby_destinations",
            joinColumns = @JoinColumn(name = "activity_id"),
            inverseJoinColumns = @JoinColumn(name = "destination_id"))
    private Set<Destination> nearbyDestinations = new HashSet<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seasonal_alternative_id")
    private Activity seasonalAlternative;

    @JsonIgnore
    public Activity getSeasonalAlternative() {
        return seasonalAlternative;
    }

    @JsonProperty("seasonalAlternativeSlug")
    public String getSeasonalAlternativeSlug() {
        return seasonalAlternative == null ? null : seasonalAlternative.getSlug();
    }

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "guide_specialty_keywords")
    private List<String> guideSpecialtyKeywords = new ArrayList<>();

    @Column(name = "responsible_travel_note", columnDefinition = "TEXT")
    private String responsibleTravelNote;

    @Column(name = "official_url", length = 1000)
    private String officialUrl;

    @Column(name = "fees_note", columnDefinition = "TEXT")
    private String feesNote;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivitySeasonMonth> seasonMonths = new ArrayList<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivitySource> sources = new ArrayList<>();

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ActivityImage> images = new ArrayList<>();

    public List<Integer> seasonMonthNumbers() {
        return seasonMonths.stream()
                .filter(ActivitySeasonMonth::isInSeason)
                .map(ActivitySeasonMonth::getMonth)
                .sorted()
                .toList();
    }

    public List<Integer> peakMonthNumbers() {
        return seasonMonths.stream()
                .filter(ActivitySeasonMonth::isPeak)
                .map(ActivitySeasonMonth::getMonth)
                .sorted()
                .toList();
    }

    public boolean inSeasonDuring(int month) {
        if (yearRound) {
            return true;
        }
        return seasonMonths.stream().anyMatch(row -> row.getMonth() == month && row.isInSeason());
    }

    public boolean peakDuring(int month) {
        return seasonMonths.stream().anyMatch(row -> row.getMonth() == month && row.isPeak());
    }
}
