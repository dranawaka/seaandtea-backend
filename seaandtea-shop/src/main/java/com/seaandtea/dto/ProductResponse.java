package com.seaandtea.dto;

import com.seaandtea.entity.Product.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private List<ProductImageDto> images;
    private BigDecimal currentPrice;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private ProductCategory category;
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean isBestSeller;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private Long id;
        private String imageUrl;
        private Boolean isPrimary;
        private String altText;
        private Integer sortOrder;
        private LocalDateTime createdAt;
    }
}
