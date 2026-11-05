package com.seaandtea.service;

import com.seaandtea.dto.*;
import com.seaandtea.entity.NewsPost;
import com.seaandtea.entity.NewsPostComment;
import com.seaandtea.entity.NewsPostLike;
import com.seaandtea.entity.User;
import com.seaandtea.entity.User.UserRole;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.NewsPostCommentRepository;
import com.seaandtea.repository.NewsPostLikeRepository;
import com.seaandtea.repository.NewsPostRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsPostService {

    private static final int BODY_SUMMARY_MAX_LENGTH = 200;

    private final NewsPostRepository newsPostRepository;
    private final NewsPostLikeRepository likeRepository;
    private final NewsPostCommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public NewsPostResponse createPost(NewsPostCreateRequest request, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NewsPost post = NewsPost.builder()
                .title(request.getTitle())
                .body(request.getBody())
                .author(author)
                .isPublished(request.getIsPublished() != null ? request.getIsPublished() : true)
                .build();
        post = newsPostRepository.save(post);
        log.info("News post created: id={} by {}", post.getId(), authorEmail);
        return toPostResponse(post, null);
    }

    @Transactional
    public NewsPostResponse updatePost(Long id, NewsPostUpdateRequest request) {
        NewsPost post = newsPostRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("NewsPost", id));
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            post.setTitle(request.getTitle());
        }
        if (request.getBody() != null) {
            post.setBody(request.getBody());
        }
        if (request.getIsPublished() != null) {
            post.setIsPublished(request.getIsPublished());
        }
        post = newsPostRepository.save(post);
        log.info("News post updated: id={}", id);
        return toPostResponse(post, null);
    }

    @Transactional
    public void deletePost(Long id) {
        if (!newsPostRepository.existsById(id)) {
            throw new ResourceNotFoundException("NewsPost", id);
        }
        likeRepository.deleteByPostId(id);
        commentRepository.deleteByPostId(id);
        newsPostRepository.deleteById(id);
        log.info("News post deleted: id={}", id);
    }

    public Page<NewsPostListResponse> getPublishedPosts(Pageable pageable, Long currentUserId) {
        return newsPostRepository.findPublishedOrderByCreatedAtDesc(pageable)
                .map(p -> toListResponse(p, currentUserId));
    }

    public Page<NewsPostListResponse> getAllPostsAdmin(Pageable pageable) {
        return newsPostRepository.findAllOrderByCreatedAtDesc(pageable)
                .map(p -> toListResponse(p, null));
    }

    public NewsPostResponse getPostById(Long id, Long currentUserId, boolean isAdmin) {
        NewsPost post = newsPostRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new ResourceNotFoundException("NewsPost", id));
        if (Boolean.FALSE.equals(post.getIsPublished()) && !isAdmin) {
            throw new ResourceNotFoundException("NewsPost", id);
        }
        List<NewsPostComment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(id, Pageable.unpaged()).getContent();
        return toPostResponseWithComments(post, currentUserId, comments);
    }

    @Transactional
    public void likePost(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NewsPost post = newsPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("NewsPost", postId));
        if (likeRepository.existsByPostIdAndUserId(postId, user.getId())) {
            throw new IllegalStateException("You have already liked this post");
        }
        NewsPostLike like = NewsPostLike.builder().post(post).user(user).build();
        likeRepository.save(like);
        log.debug("Post {} liked by user {}", postId, userEmail);
    }

    @Transactional
    public void unlikePost(Long postId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NewsPostLike like = likeRepository.findByPostIdAndUserId(postId, user.getId())
                .orElseThrow(() -> new IllegalStateException("You have not liked this post"));
        likeRepository.delete(like);
        log.debug("Post {} unliked by user {}", postId, userEmail);
    }

    @Transactional
    public NewsPostCommentResponse addComment(Long postId, NewsPostCommentCreateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        NewsPost post = newsPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("NewsPost", postId));
        if (Boolean.FALSE.equals(post.getIsPublished())) {
            throw new ResourceNotFoundException("NewsPost", postId);
        }
        NewsPostComment comment = NewsPostComment.builder()
                .post(post)
                .user(user)
                .text(request.getText())
                .build();
        comment = commentRepository.save(comment);
        log.info("Comment added to post {} by {}", postId, userEmail);
        return toCommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!commentRepository.existsByPostIdAndId(postId, commentId)) {
            throw new ResourceNotFoundException("Comment", commentId);
        }
        NewsPostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));
        boolean isAdmin = user.getRole() == UserRole.ADMIN;
        if (!isAdmin && !comment.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only delete your own comment");
        }
        commentRepository.delete(comment);
        log.info("Comment {} deleted from post {} by {}", commentId, postId, userEmail);
    }

    public Page<NewsPostCommentResponse> getComments(Long postId, Pageable pageable) {
        if (!newsPostRepository.existsById(postId)) {
            throw new ResourceNotFoundException("NewsPost", postId);
        }
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId, pageable)
                .map(this::toCommentResponse);
    }

    private String authorDisplayName(User author) {
        return author.getFirstName() + " " + author.getLastName();
    }

    private String bodySummary(String body) {
        if (body == null) return "";
        if (body.length() <= BODY_SUMMARY_MAX_LENGTH) return body;
        return body.substring(0, BODY_SUMMARY_MAX_LENGTH) + "...";
    }

    private boolean isLikedByUser(Long postId, Long userId) {
        return userId != null && likeRepository.existsByPostIdAndUserId(postId, userId);
    }

    private NewsPostListResponse toListResponse(NewsPost post, Long currentUserId) {
        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());
        return NewsPostListResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .bodySummary(bodySummary(post.getBody()))
                .authorId(post.getAuthor().getId())
                .authorDisplayName(authorDisplayName(post.getAuthor()))
                .isPublished(Boolean.TRUE.equals(post.getIsPublished()))
                .createdAt(post.getCreatedAt())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .likedByCurrentUser(isLikedByUser(post.getId(), currentUserId))
                .build();
    }

    private NewsPostResponse toPostResponse(NewsPost post, Long currentUserId) {
        long likeCount = likeRepository.countByPostId(post.getId());
        long commentCount = commentRepository.countByPostId(post.getId());
        return NewsPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .body(post.getBody())
                .authorId(post.getAuthor().getId())
                .authorDisplayName(authorDisplayName(post.getAuthor()))
                .isPublished(Boolean.TRUE.equals(post.getIsPublished()))
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .likeCount(likeCount)
                .commentCount(commentCount)
                .likedByCurrentUser(isLikedByUser(post.getId(), currentUserId))
                .comments(null)
                .build();
    }

    private NewsPostResponse toPostResponseWithComments(NewsPost post, Long currentUserId, List<NewsPostComment> comments) {
        NewsPostResponse resp = toPostResponse(post, currentUserId);
        resp.setComments(comments.stream().map(this::toCommentResponse).collect(Collectors.toList()));
        return resp;
    }

    private NewsPostCommentResponse toCommentResponse(NewsPostComment c) {
        return NewsPostCommentResponse.builder()
                .id(c.getId())
                .postId(c.getPost().getId())
                .userId(c.getUser().getId())
                .userDisplayName(authorDisplayName(c.getUser()))
                .text(c.getText())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
