package com.seaandtea.service;

import com.seaandtea.dto.OverallRatingResponse;
import com.seaandtea.dto.ReviewCreateRequest;
import com.seaandtea.dto.ReviewResponse;
import com.seaandtea.entity.Booking;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.Review;
import com.seaandtea.entity.User;
import com.seaandtea.repository.BookingRepository;
import com.seaandtea.repository.GuideRepository;
import com.seaandtea.repository.ReviewRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final GuideRepository guideRepository;

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = bookingRepository.findByIdAndTouristIdAndStatus(
                        request.getBookingId(),
                        user.getId(),
                        Booking.BookingStatus.COMPLETED)
                .orElseThrow(() -> new IllegalStateException(
                        "Booking not found, or not completed, or you are not the tourist for this booking"));

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new IllegalStateException("You have already submitted a review for this booking");
        }

        Review review = Review.builder()
                .booking(booking)
                .tourist(user)
                .guide(booking.getGuide())
                .tour(booking.getTour())
                .rating(request.getRating())
                .comment(request.getComment())
                .isVerified(false)
                .build();
        review = reviewRepository.save(review);

        updateGuideRatingStats(booking.getGuide().getId());

        log.info("Review created for booking {} by user {}", booking.getId(), userEmail);
        return toReviewResponse(review);
    }

    public Page<ReviewResponse> getReviewsByTourId(Long tourId, Pageable pageable) {
        return reviewRepository.findByTourIdOrderByCreatedAtDesc(tourId, pageable)
                .map(this::toReviewResponse);
    }

    public Page<ReviewResponse> getReviewsByGuideId(Long guideId, Pageable pageable) {
        return reviewRepository.findByGuideIdOrderByCreatedAtDesc(guideId, pageable)
                .map(this::toReviewResponse);
    }

    public OverallRatingResponse getOverallRatingForTour(Long tourId) {
        Double avg = reviewRepository.getAverageRatingByTourId(tourId);
        long count = reviewRepository.countByTourId(tourId);
        Map<Integer, Long> breakdown = ratingBreakdownForTour(tourId);
        return OverallRatingResponse.of(avg != null ? avg : 0.0, count, breakdown);
    }

    public OverallRatingResponse getOverallRatingForGuide(Long guideId) {
        Double avg = reviewRepository.getAverageRatingByGuideId(guideId);
        long count = reviewRepository.countByGuideId(guideId);
        Map<Integer, Long> breakdown = ratingBreakdownForGuide(guideId);
        return OverallRatingResponse.of(avg != null ? avg : 0.0, count, breakdown);
    }

    private Map<Integer, Long> ratingBreakdownForTour(Long tourId) {
        Map<Integer, Long> map = new HashMap<>();
        for (int r = 1; r <= 5; r++) {
            map.put(r, reviewRepository.countByTourIdAndRating(tourId, r));
        }
        return map;
    }

    private Map<Integer, Long> ratingBreakdownForGuide(Long guideId) {
        Map<Integer, Long> map = new HashMap<>();
        for (int r = 1; r <= 5; r++) {
            map.put(r, reviewRepository.countByGuideIdAndRating(guideId, r));
        }
        return map;
    }

    @Transactional
    public void updateGuideRatingStats(Long guideId) {
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found"));
        Double avg = reviewRepository.getAverageRatingByGuideId(guideId);
        long count = reviewRepository.countByGuideId(guideId);
        guide.setAverageRating(avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        guide.setTotalReviews((int) count);
        guideRepository.save(guide);
    }

    private ReviewResponse toReviewResponse(Review r) {
        String touristName = r.getTourist().getFirstName() + " " + r.getTourist().getLastName();
        return ReviewResponse.builder()
                .id(r.getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .touristName(touristName)
                .isVerified(Boolean.TRUE.equals(r.getIsVerified()))
                .createdAt(r.getCreatedAt())
                .tourId(r.getTour().getId())
                .guideId(r.getGuide().getId())
                .build();
    }
}
