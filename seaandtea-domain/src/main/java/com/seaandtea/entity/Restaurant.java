package com.seaandtea.entity;

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
@Table(name = "restaurants", indexes = {
        @Index(name = "idx_restaurants_status_destination", columnList = "status, destination_id"),
        @Index(name = "idx_restaurants_status_price_tier", columnList = "status, price_tier")
})
@Getter
@Setter
public class Restaurant extends ThingBase {

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "restaurant_cuisines", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "cuisine", nullable = false, length = 40)
    private Set<Cuisine> cuisines = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "price_tier", length = 20)
    private PriceTier priceTier;

    @Column(name = "price_note", length = 500)
    private String priceNote;

    @Column(name = "address_line", length = 500)
    private String addressLine;

    @Column(length = 200)
    private String neighbourhood;

    @Column(length = 200)
    private String phone;

    @Column(length = 40)
    private String whatsapp;

    @Column(name = "website_url", length = 1000)
    private String websiteUrl;

    @Column(name = "reservations_url", length = 1000)
    private String reservationsUrl;

    @Column(name = "reservations_email", length = 255)
    private String reservationsEmail;

    @Column(name = "opening_hours_text", columnDefinition = "TEXT")
    private String openingHoursText;

    @Column(name = "opening_hours_json", columnDefinition = "TEXT")
    private String openingHoursJson;

    @Column(name = "closed_on_poya", nullable = false)
    private boolean closedOnPoya;

    @Column(name = "serves_alcohol")
    private Boolean servesAlcohol;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "restaurant_dietary", joinColumns = @JoinColumn(name = "restaurant_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "dietary", nullable = false, length = 40)
    private Set<Dietary> dietary = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "good_for")
    private List<String> goodFor = new ArrayList<>();

    @Column(name = "google_place_id", length = 255)
    private String googlePlaceId;

    @Column(name = "maps_url", length = 1000)
    private String mapsUrl;

    @Column(name = "guide_tip", columnDefinition = "TEXT")
    private String guideTip;

    @Column(name = "recommended_by_guide_id")
    private Long recommendedByGuideId;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RestaurantSource> sources = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<RestaurantImage> images = new ArrayList<>();
}
