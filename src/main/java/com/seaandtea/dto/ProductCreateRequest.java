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
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 200)
    private String name;

    @Size(max = 2000)
    private String description;

    /** Image URLs. First image or primaryImageIndex is used as primary. */
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<@Size(max = 500) String> imageUrls;

    /** Index in imageUrls of the primary image (0-based). Default 0. */
    private Integer primaryImageIndex = 0;

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "99999.99", message = "Price cannot exceed 99,999.99")
    private BigDecimal currentPrice;

    @DecimalMin(value = "0.01")
    @DecimalMax(value = "99999.99")
    private BigDecimal originalPrice;

    @NotNull(message = "Category is required")
    private ProductCategory category;

    @DecimalMin(value = "0")
    @DecimalMax(value = "5")
    private BigDecimal rating;

    @Min(0)
    private Integer reviewCount = 0;

    private Boolean isBestSeller = false;

    private Boolean isActive = true;
}
