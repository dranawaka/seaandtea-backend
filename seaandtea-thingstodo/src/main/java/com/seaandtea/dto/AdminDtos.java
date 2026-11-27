package com.seaandtea.dto;

import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ActivityRegion;
import com.seaandtea.entity.Cuisine;
import com.seaandtea.entity.DateStatus;
import com.seaandtea.entity.Dietary;
import com.seaandtea.entity.Difficulty;
import com.seaandtea.entity.EventType;
import com.seaandtea.entity.PriceTier;
import com.seaandtea.entity.RecurrenceType;
import com.seaandtea.entity.SourceKind;
import com.seaandtea.entity.ThingStatus;
import com.seaandtea.entity.ThingType;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.Tradition;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class AdminDtos {

    private AdminDtos() {
    }

    @Data
    public static class SourceInput {
        private String url;
        private String label;
        private SourceKind kind;
    }

    @Data
    public static class OccurrenceInput {
        private Integer year;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private DateStatus dateStatus;
        private String statusSourceUrl;
        private String ticketUrl;
        private String notes;
    }

    @Data
    public static class ActivityInput {
        private String slug;
        private String title;
        private String summary;
        private String description;
        private String destinationSlug;
        private ActivityCategory category;
        private ActivityRegion region;
        private boolean yearRound;
        private List<Integer> seasonMonths = new ArrayList<>();
        private List<Integer> peakMonths = new ArrayList<>();
        private Difficulty difficulty;
        private String seasonNote;
        private Integer typicalDurationMinutes;
        private String bestTimeOfDay;
        private List<Tour.TourCategory> tourCategories = new ArrayList<>();
        private List<String> nearbyDestinationSlugs = new ArrayList<>();
        private String seasonalAlternativeSlug;
        private String responsibleTravelNote;
        private String officialUrl;
        private String feesNote;
        private String heroImageUrl;
        private Double latitude;
        private Double longitude;
        private String seoTitle;
        private String seoDescription;
        private String needsVerification;
        private ThingStatus status;
        private List<SourceInput> sources = new ArrayList<>();
    }

    @Data
    public static class EventInput {
        private String slug;
        private String name;
        private String summary;
        private String description;
        private String destinationSlug;
        private EventType type;
        private Tradition tradition;
        private RecurrenceType recurrenceType;
        private String poyaName;
        private Integer typicalStartMonth;
        private Integer typicalEndMonth;
        private Integer typicalDurationDays;
        private String venueName;
        private String venueAddress;
        private boolean free;
        private String officialUrl;
        private String etiquetteNote;
        private String crowdNote;
        private String heroImageUrl;
        private Double latitude;
        private Double longitude;
        private String seoTitle;
        private String seoDescription;
        private String needsVerification;
        private ThingStatus status;
        private List<SourceInput> sources = new ArrayList<>();
        private List<OccurrenceInput> occurrences = new ArrayList<>();
    }

    @Data
    public static class RestaurantInput {
        private String slug;
        private String name;
        private String summary;
        private String description;
        private String destinationSlug;
        private List<Cuisine> cuisines = new ArrayList<>();
        private PriceTier priceTier;
        private String priceNote;
        private String addressLine;
        private String neighbourhood;
        private String phone;
        private String whatsapp;
        private String websiteUrl;
        private String reservationsUrl;
        private String reservationsEmail;
        private String openingHoursText;
        private String openingHoursJson;
        private boolean closedOnPoya;
        private Boolean servesAlcohol;
        private List<Dietary> dietary = new ArrayList<>();
        private List<String> goodFor = new ArrayList<>();
        private String googlePlaceId;
        private String mapsUrl;
        private String guideTip;
        private String heroImageUrl;
        private Double latitude;
        private Double longitude;
        private String seoTitle;
        private String seoDescription;
        private String needsVerification;
        private ThingStatus status;
        private List<SourceInput> sources = new ArrayList<>();
    }

    @Data
    public static class StatusInput {
        private ThingStatus status;
        private String moderationNote;
        private Boolean markVerified;
    }

    @Data
    public static class AdminRow {
        private Long id;
        private ThingType type;
        private String slug;
        private String title;
        private String destination;
        private ThingStatus status;
        private String needsVerification;
        private LocalDateTime lastVerifiedAt;
    }
}
