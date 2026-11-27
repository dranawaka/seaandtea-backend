package com.seaandtea.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ActivityRegion;
import com.seaandtea.entity.Cuisine;
import com.seaandtea.entity.DateStatus;
import com.seaandtea.entity.DestinationRegion;
import com.seaandtea.entity.Dietary;
import com.seaandtea.entity.Difficulty;
import com.seaandtea.entity.EventType;
import com.seaandtea.entity.PriceTier;
import com.seaandtea.entity.RecurrenceType;
import com.seaandtea.entity.SourceKind;
import com.seaandtea.entity.ThingType;
import com.seaandtea.entity.Tradition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public final class ThingDtos {

    private ThingDtos() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Ref {
        private String slug;
        private String name;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestinationCard {
        private String slug;
        private String name;
        private DestinationRegion region;
        private String heroImageUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceView {
        private String url;
        private String label;
        private SourceKind kind;
        private LocalDateTime accessedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GalleryImage {
        private String url;
        private String altText;
        private String credit;
        private String licence;
        private String sourceUrl;
        private Integer sortOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityCard {
        private String slug;
        private String title;
        private ActivityCategory category;
        private ActivityRegion region;
        private Ref destination;
        private String heroImageUrl;
        private String summary;
        private Difficulty difficulty;
        private List<Integer> seasonMonths;
        private List<Integer> peakMonths;
        private boolean yearRound;
        private boolean inSeasonNow;
        @JsonProperty("isPeakNow")
        private boolean peakNow;
        private Ref seasonalAlternative;
        private int linkedTourCount;
        private BigDecimal fromPriceUsd;

        @JsonIgnore
        public boolean isPeakNow() {
            return peakNow;
        }

        @JsonProperty("isPeakNow")
        public boolean getIsPeakNow() {
            return peakNow;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityDetail {
        private String slug;
        private String title;
        private ActivityCategory category;
        private ActivityRegion region;
        private Ref destination;
        private String heroImageUrl;
        private String summary;
        private Difficulty difficulty;
        private List<Integer> seasonMonths;
        private List<Integer> peakMonths;
        private boolean yearRound;
        private boolean inSeasonNow;
        @JsonProperty("isPeakNow")
        private boolean peakNow;
        private Ref seasonalAlternative;
        private int linkedTourCount;
        private BigDecimal fromPriceUsd;
        private String description;
        private String seasonNote;
        private Integer typicalDurationMinutes;
        private String bestTimeOfDay;
        private String responsibleTravelNote;
        private String officialUrl;
        private String feesNote;
        private List<Ref> nearbyDestinations;
        private List<GalleryImage> gallery;
        private List<SourceView> sources;
        private LocalDateTime lastVerifiedAt;
        private String seoTitle;
        private String seoDescription;
        private Double latitude;
        private Double longitude;

        @JsonIgnore
        public boolean isPeakNow() {
            return peakNow;
        }

        @JsonProperty("isPeakNow")
        public boolean getIsPeakNow() {
            return peakNow;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OccurrenceCard {
        private Integer year;
        private LocalDate startDate;
        private LocalDate endDate;
        private DateStatus dateStatus;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OccurrenceDetail {
        private Long id;
        private Integer year;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private DateStatus dateStatus;
        private String statusSourceUrl;
        private LocalDateTime sourceCheckedAt;
        private String ticketUrl;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventCard {
        private String slug;
        private String name;
        private EventType type;
        private Tradition tradition;
        private Ref destination;
        private String venueName;
        private String heroImageUrl;
        private String summary;
        private RecurrenceType recurrenceType;
        private Integer typicalStartMonth;
        private Integer typicalEndMonth;
        private OccurrenceCard nextOccurrence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDetail {
        private String slug;
        private String name;
        private EventType type;
        private Tradition tradition;
        private Ref destination;
        private String venueName;
        private String heroImageUrl;
        private String summary;
        private RecurrenceType recurrenceType;
        private Integer typicalStartMonth;
        private Integer typicalEndMonth;
        private OccurrenceCard nextOccurrence;
        private String description;
        private String etiquetteNote;
        private String crowdNote;
        private String officialUrl;
        private boolean free;
        private List<OccurrenceDetail> occurrences;
        private List<GalleryImage> gallery;
        private List<SourceView> sources;
        private LocalDateTime lastVerifiedAt;
        private String seoTitle;
        private String seoDescription;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantCard {
        private String slug;
        private String name;
        private List<Cuisine> cuisines;
        private PriceTier priceTier;
        private String priceNote;
        private Ref destination;
        private String neighbourhood;
        private String heroImageUrl;
        private String summary;
        private List<Dietary> dietary;
        private String openingHoursText;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RestaurantDetail {
        private String slug;
        private String name;
        private List<Cuisine> cuisines;
        private PriceTier priceTier;
        private String priceNote;
        private Ref destination;
        private String neighbourhood;
        private String heroImageUrl;
        private String summary;
        private List<Dietary> dietary;
        private String openingHoursText;
        private String addressLine;
        private String phone;
        private String whatsapp;
        private String websiteUrl;
        private String reservationsUrl;
        private boolean closedOnPoya;
        private Boolean servesAlcohol;
        private String mapsUrl;
        private String googlePlaceId;
        private String guideTip;
        private List<GalleryImage> gallery;
        private List<SourceView> sources;
        private LocalDateTime lastVerifiedAt;
        private Double latitude;
        private Double longitude;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GuideChip {
        private Long id;
        private String firstName;
        private String profilePictureUrl;
        private BigDecimal averageRating;
        @JsonProperty("isVerified")
        private boolean verified;

        @JsonIgnore
        public boolean isVerified() {
            return verified;
        }

        @JsonProperty("isVerified")
        public boolean getIsVerified() {
            return verified;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThingTours {
        private List<TourListResponse> tours;
        private List<GuideChip> guides;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int month;
        private List<ActivityCard> inSeasonActivities;
        private List<EventCard> upcomingEvents;
        private List<RestaurantCard> featuredRestaurants;
        private Map<ActivityCategory, Long> categoryCounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DestinationBundle {
        private DestinationCard destination;
        private List<ActivityCard> activities;
        private List<EventCard> events;
        private List<RestaurantCard> restaurants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SitemapEntry {
        private ThingType type;
        private String slug;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TourLinkRequest {
        private ThingType type;
        private Long id;
    }
}
