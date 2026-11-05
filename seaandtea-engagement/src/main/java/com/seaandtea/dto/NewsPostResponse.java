package com.seaandtea.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsPostResponse {

    private Long id;
    private String title;
    private String body;
    private Long authorId;
    private String authorDisplayName;
    private Boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long likeCount;
    private long commentCount;
    private Boolean likedByCurrentUser;
    private List<NewsPostCommentResponse> comments;
}
