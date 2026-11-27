package com.seaandtea.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "activity_season_month", uniqueConstraints = @UniqueConstraint(
        name = "uk_activity_season_month", columnNames = {"activity_id", "month_number"}))
@Getter
@Setter
public class ActivitySeasonMonth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(name = "month_number", nullable = false)
    private Integer month;

    @Column(name = "in_season", nullable = false)
    private boolean inSeason;

    @Column(nullable = false)
    private boolean peak;
}
