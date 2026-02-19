package com.seaandtea.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "current_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "original_price", precision = 10, scale = 2)
    private BigDecimal originalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "is_best_seller")
    @Builder.Default
    private Boolean isBestSeller = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getDiscountPercentage() {
        if (originalPrice == null || originalPrice.compareTo(BigDecimal.ZERO) <= 0
                || currentPrice.compareTo(originalPrice) >= 0) {
            return null;
        }
        return originalPrice.subtract(currentPrice)
                .multiply(BigDecimal.valueOf(100))
                .divide(originalPrice, 0, java.math.RoundingMode.HALF_UP)
                .intValue();
    }

    public enum ProductCategory {
        /** All Products (filter only; use null or omit for "all") */
        ALL("all"),
        /** Sea & Beach Wears and Handy Crafts */
        SEA("sea"),
        /** Premium Tea */
        TEA("tea"),
        /** Spices & Food */
        SPICES("spices"),
        /** Clothing & Textiles */
        CLOTHING("clothing"),
        /** Souvenirs & Crafts */
        SOUVENIRS("souvenirs"),
        /** Beauty & Wellness */
        BEAUTY("beauty"),
        OTHER("other");

        private final String slug;

        ProductCategory(String slug) {
            this.slug = slug;
        }

        @JsonValue
        public String getSlug() {
            return slug;
        }

        @JsonCreator
        public static ProductCategory fromSlug(String value) {
            if (value == null || value.isBlank()) return null;
            String lower = value.toLowerCase();
            for (ProductCategory c : values()) {
                if (c.slug.equals(lower)) return c;
            }
            throw new IllegalArgumentException("Unknown product category: '" + value + "'. Accepted: all, sea, tea, spices, clothing, souvenirs, beauty, other");
        }
    }
}
