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
public class NewsPostListResponse {

    private Long id;
    private String title;
    private String bodySummary;
    private Long authorId;
    private String authorDisplayName;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private long likeCount;
    private long commentCount;
    private Boolean likedByCurrentUser;
}
