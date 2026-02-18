package com.seaandtea.controller;

import com.seaandtea.dto.OverallRatingResponse;
import com.seaandtea.dto.ReviewCreateRequest;
import com.seaandtea.dto.ReviewResponse;
import com.seaandtea.service.ReviewService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Reviews & Ratings", description = "Rate and comment on tours/guides; get overall rating")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(
        summary = "Submit a review",
        description = "Submit a rating and optional comment for a completed booking. Only the tourist of the booking can submit.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created",
                    content = @Content(schema = @Schema(implementation = ReviewResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Already reviewed this booking")
    })
    public ResponseEntity<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request,
            Authentication authentication) {
        log.info("Creating review for booking: {}", request.getBookingId());
        ReviewResponse response = reviewService.createReview(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "List reviews by tour or guide",
        description = "Get paginated reviews. Provide either tourId or guideId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved")
    })
    public ResponseEntity<Page<ReviewResponse>> getReviews(
            @Parameter(description = "Filter by tour ID") @RequestParam(required = false) Long tourId,
            @Parameter(description = "Filter by guide ID") @RequestParam(required = false) Long guideId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (tourId != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return ResponseEntity.ok(reviewService.getReviewsByTourId(tourId, pageable));
        }
        if (guideId != null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return ResponseEntity.ok(reviewService.getReviewsByGuideId(guideId, pageable));
        }
        throw new IllegalArgumentException("Provide either tourId or guideId");
    }

    @GetMapping("/rating")
    @Operation(
        summary = "Get overall rating",
        description = "Get calculated overall rating (average and total count) for a tour or guide. Provide either tourId or guideId."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Overall rating",
                    content = @Content(schema = @Schema(implementation = OverallRatingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Must provide tourId or guideId")
    })
    public ResponseEntity<OverallRatingResponse> getOverallRating(
            @Parameter(description = "Tour ID for tour rating") @RequestParam(required = false) Long tourId,
            @Parameter(description = "Guide ID for guide rating") @RequestParam(required = false) Long guideId) {

        if (tourId != null) {
            return ResponseEntity.ok(reviewService.getOverallRatingForTour(tourId));
        }
        if (guideId != null) {
            return ResponseEntity.ok(reviewService.getOverallRatingForGuide(guideId));
        }
        throw new IllegalArgumentException("Provide either tourId or guideId");
    }
}
