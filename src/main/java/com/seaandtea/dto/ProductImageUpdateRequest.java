package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImageUpdateRequest {

    /** Set this image as the primary (main) image for the product. */
    private Boolean isPrimary;

    /** Display order; lower values first. */
    private Integer sortOrder;

    /** Alt text for accessibility. */
    private String altText;
}
