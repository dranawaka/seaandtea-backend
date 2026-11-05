package com.seaandtea.controller;

import com.seaandtea.dto.*;
import com.seaandtea.entity.User;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.service.NewsPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/news")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "News & Posts", description = "Admin creates news posts; customers and guides can like and comment")
public class NewsPostController {

    private final NewsPostService newsPostService;
    private final UserRepository userRepository;

    private Long currentUserIdOrNull(Authentication auth) {
        if (auth == null || auth.getName() == null) return null;
        return userRepository.findByEmail(auth.getName()).map(User::getId).orElse(null);
    }

    private boolean isAdmin(Authentication auth) {
        if (auth == null) return false;
        return userRepository.findByEmail(auth.getName())
                .map(u -> u.getRole() == User.UserRole.ADMIN).orElse(false);
    }

    // ---------- Public (published posts) ----------

    @GetMapping
    @Operation(summary = "List published news posts", description = "Get paginated list of published posts. Optional auth for likedByCurrentUser.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts retrieved")
    })
    public ResponseEntity<Page<NewsPostListResponse>> getPublishedPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NewsPostListResponse> response = newsPostService.getPublishedPosts(pageable, currentUserIdOrNull(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get post by ID", description = "Get a single published post with comments. Unpublished only visible to admin.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post found", content = @Content(schema = @Schema(implementation = NewsPostResponse.class))),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<NewsPostResponse> getPostById(
            @PathVariable Long id,
            Authentication authentication) {
        NewsPostResponse response = newsPostService.getPostById(id, currentUserIdOrNull(authentication), isAdmin(authentication));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/comments")
    @Operation(summary = "Get comments for a post", description = "Paginated comments for a post")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comments retrieved"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Page<NewsPostCommentResponse>> getComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
        return ResponseEntity.ok(newsPostService.getComments(id, pageable));
    }

    // ---------- Admin: CRUD ----------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create news post (Admin)", description = "Create a new news post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Post created", content = @Content(schema = @Schema(implementation = NewsPostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<NewsPostResponse> createPost(
            @Valid @RequestBody NewsPostCreateRequest request,
            Authentication authentication) {
        NewsPostResponse response = newsPostService.createPost(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update news post (Admin)", description = "Update an existing post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Post updated", content = @Content(schema = @Schema(implementation = NewsPostResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<NewsPostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody NewsPostUpdateRequest request) {
        NewsPostResponse response = newsPostService.updatePost(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete news post (Admin)", description = "Permanently delete a post and its likes/comments", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Post deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        newsPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all posts (Admin)", description = "Get all posts including unpublished", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Posts retrieved"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<Page<NewsPostListResponse>> getAllPostsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(newsPostService.getAllPostsAdmin(pageable));
    }

    // ---------- Like / Comment (customers & guides) ----------

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(summary = "Like a post", description = "Like a published post. One like per user per post.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Post liked"),
        @ApiResponse(responseCode = "409", description = "Already liked this post"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Void> likePost(
            @Parameter(description = "Post ID") @PathVariable Long id,
            Authentication authentication) {
        newsPostService.likePost(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(summary = "Unlike a post", description = "Remove your like from the post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Post unliked"),
        @ApiResponse(responseCode = "409", description = "You have not liked this post"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<Void> unlikePost(
            @PathVariable Long id,
            Authentication authentication) {
        newsPostService.unlikePost(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(summary = "Add a comment", description = "Add a comment to a published post", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Comment created", content = @Content(schema = @Schema(implementation = NewsPostCommentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "404", description = "Post not found")
    })
    public ResponseEntity<NewsPostCommentResponse> addComment(
            @PathVariable Long id,
            @Valid @RequestBody NewsPostCommentCreateRequest request,
            Authentication authentication) {
        NewsPostCommentResponse response = newsPostService.addComment(id, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}/comments/{commentId}")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(summary = "Delete a comment", description = "Delete your own comment; admin can delete any.", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Comment deleted"),
        @ApiResponse(responseCode = "403", description = "Can only delete your own comment"),
        @ApiResponse(responseCode = "404", description = "Post or comment not found")
    })
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication) {
        newsPostService.deleteComment(id, commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
