package com.seaandtea.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "event_occurrences",
        uniqueConstraints = @UniqueConstraint(name = "uk_event_occurrence_year", columnNames = {"event_id", "year_number"}),
        indexes = @Index(name = "idx_event_occurrences_event_year", columnList = "event_id, year_number"))
@Getter
@Setter
public class EventOccurrence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "year_number", nullable = false)
    private Integer year;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_status", nullable = false, length = 20)
    private DateStatus dateStatus;

    @Column(name = "status_source_url", length = 1000)
    private String statusSourceUrl;

    @Column(name = "source_checked_at")
    private LocalDateTime sourceCheckedAt;

    @Column(name = "ticket_url", length = 1000)
    private String ticketUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @JsonIgnore
    public Event getEvent() {
        return event;
    }
}
