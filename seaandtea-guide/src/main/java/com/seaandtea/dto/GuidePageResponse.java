package com.seaandtea.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Public page payload. Spring Data {@code Page} fields are not annotated for
 * {@link ApiViews.Public}, and Spring disables default view inclusion, so a
 * raw {@code Page} serializes as {@code {}}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuidePageResponse {

    @JsonView(ApiViews.Public.class)
    private List<GuideProfileResponse> content;

    @JsonView(ApiViews.Public.class)
    private int totalPages;

    @JsonView(ApiViews.Public.class)
    private long totalElements;

    @JsonView(ApiViews.Public.class)
    private int number;

    @JsonView(ApiViews.Public.class)
    private int size;

    public static GuidePageResponse from(Page<GuideProfileResponse> page) {
        return GuidePageResponse.builder()
                .content(page.getContent())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .number(page.getNumber())
                .size(page.getSize())
                .build();
    }
}
