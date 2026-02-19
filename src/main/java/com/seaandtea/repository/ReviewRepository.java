package com.seaandtea.repository;

import com.seaandtea.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Modifying
    @Query("DELETE FROM Review r WHERE r.tourist.id = :userId")
    void deleteByTouristId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.guide.id = :guideId")
    void deleteByGuideId(@Param("guideId") Long guideId);

    Optional<Review> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    @Query("SELECT r FROM Review r JOIN FETCH r.tourist WHERE r.tour.id = :tourId ORDER BY r.createdAt DESC")
    Page<Review> findByTourIdOrderByCreatedAtDesc(@Param("tourId") Long tourId, Pageable pageable);

    @Query("SELECT r FROM Review r JOIN FETCH r.tourist WHERE r.guide.id = :guideId ORDER BY r.createdAt DESC")
    Page<Review> findByGuideIdOrderByCreatedAtDesc(@Param("guideId") Long guideId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.tour.id = :tourId")
    Double getAverageRatingByTourId(@Param("tourId") Long tourId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tour.id = :tourId")
    Long countByTourId(@Param("tourId") Long tourId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.guide.id = :guideId")
    Double getAverageRatingByGuideId(@Param("guideId") Long guideId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.guide.id = :guideId")
    Long countByGuideId(@Param("guideId") Long guideId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tour.id = :tourId AND r.rating = :rating")
    Long countByTourIdAndRating(@Param("tourId") Long tourId, @Param("rating") Integer rating);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.guide.id = :guideId AND r.rating = :rating")
    Long countByGuideIdAndRating(@Param("guideId") Long guideId, @Param("rating") Integer rating);
}
