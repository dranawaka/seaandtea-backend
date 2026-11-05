package com.seaandtea.dto;

import com.seaandtea.entity.Product.ProductCategory;
import jakarta.validation.constraints.*;
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
public class ProductUpdateRequest {

    @Size(min = 2, max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<@Size(max = 500) String> imageUrls;

    private Integer primaryImageIndex;

    @DecimalMin(value = "0.01")
    @DecimalMax(value = "99999.99")
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.01")
    @DecimalMax(value = "99999.99")
    private BigDecimal originalPrice;

    private ProductCategory category;

    @DecimalMin(value = "0")
    @DecimalMax(value = "5")
    private BigDecimal rating;

    @Min(0)
    private Integer reviewCount;

    private Boolean isBestSeller;

    private Boolean isActive;
}
