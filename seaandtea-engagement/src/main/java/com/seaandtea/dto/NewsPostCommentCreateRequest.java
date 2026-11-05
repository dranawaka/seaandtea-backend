package com.seaandtea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsPostCommentCreateRequest {

    @NotBlank(message = "Comment text is required")
    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String text;
}
