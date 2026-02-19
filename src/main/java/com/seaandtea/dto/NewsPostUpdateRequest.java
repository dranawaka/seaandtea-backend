package com.seaandtea.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsPostUpdateRequest {

    @Size(max = 500, message = "Title cannot exceed 500 characters")
    private String title;

    @Size(max = 50_000, message = "Body cannot exceed 50000 characters")
    private String body;

    private Boolean isPublished;
}
