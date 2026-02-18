package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverallRatingResponse {

    /** Overall average rating (1-5), rounded to 2 decimal places */
    private BigDecimal averageRating;
    /** Total number of ratings */
    private Long totalCount;

    /** Optional: count per star (1-5) for breakdown. Keys are "1", "2", "3", "4", "5". */
    private java.util.Map<Integer, Long> ratingBreakdown;

    public static OverallRatingResponse of(double average, long totalCount, java.util.Map<Integer, Long> breakdown) {
        BigDecimal avg = totalCount == 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
        return OverallRatingResponse.builder()
                .averageRating(avg)
                .totalCount(totalCount)
                .ratingBreakdown(breakdown != null ? breakdown : java.util.Collections.emptyMap())
                .build();
    }
}
