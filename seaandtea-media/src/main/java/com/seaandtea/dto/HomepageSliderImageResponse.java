package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomepageSliderImageResponse {

    private Long id;
    private String imageUrl;
    private Integer sortOrder;
    private String altText;
    private LocalDateTime createdAt;
}
