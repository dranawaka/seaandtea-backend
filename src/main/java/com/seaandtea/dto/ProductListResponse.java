package com.seaandtea.dto;

import com.seaandtea.entity.Product.ProductCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListResponse {

    private Long id;
    private String name;
    private String description;
    private List<String> imageUrls;
    private BigDecimal currentPrice;
    private BigDecimal originalPrice;
    private Integer discountPercentage;
    private ProductCategory category;
    private BigDecimal rating;
    private Integer reviewCount;
    private Boolean isBestSeller;
}
