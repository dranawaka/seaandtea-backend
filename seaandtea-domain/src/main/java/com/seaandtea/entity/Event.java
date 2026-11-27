package com.seaandtea.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events", indexes = {
        @Index(name = "idx_events_status_destination", columnList = "status, destination_id")
})
@Getter
@Setter
public class Event extends ThingBase {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tradition tradition;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false, length = 20)
    private RecurrenceType recurrenceType;

    @Column(name = "poya_name", length = 40)
    private String poyaName;

    @Column(name = "typical_start_month")
    private Integer typicalStartMonth;

    @Column(name = "typical_end_month")
    private Integer typicalEndMonth;

    @Column(name = "typical_duration_days")
    private Integer typicalDurationDays;

    @Column(name = "venue_name", length = 500)
    private String venueName;

    @Column(name = "venue_address", length = 500)
    private String venueAddress;

    @Column(name = "is_free", nullable = false)
    private boolean free;

    @Column(name = "official_url", length = 1000)
    private String officialUrl;

    @Column(name = "etiquette_note", columnDefinition = "TEXT")
    private String etiquetteNote;

    @Column(name = "crowd_note", columnDefinition = "TEXT")
    private String crowdNote;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("year ASC")
    private List<EventOccurrence> occurrences = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventSource> sources = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<EventImage> images = new ArrayList<>();
}
