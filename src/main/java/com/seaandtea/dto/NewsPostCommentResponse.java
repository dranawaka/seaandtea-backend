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
public class NewsPostCommentResponse {

    private Long id;
    private Long postId;
    private Long userId;
    private String userDisplayName;
    private String text;
    private LocalDateTime createdAt;
}
