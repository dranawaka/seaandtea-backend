package com.seaandtea.repository;

import com.seaandtea.entity.Tour;
import com.seaandtea.entity.Tour.TourCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourRepository extends JpaRepository<Tour, Long> {
    
    // Find tours by guide
    @Query("SELECT t FROM Tour t WHERE t.guide.id = :guideId AND t.isActive = true")
    List<Tour> findActiveByGuideId(@Param("guideId") Long guideId);
    
    // Find tours by category
    @Query("SELECT t FROM Tour t WHERE t.category = :category AND t.isActive = true")
    Page<Tour> findActiveByCategoryOrderByCreatedAtDesc(@Param("category") TourCategory category, Pageable pageable);
    
    // Search tours by title or description
    @Query("SELECT t FROM Tour t WHERE t.isActive = true AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Tour> searchActiveToursOrderByCreatedAtDesc(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Find tours by price range
    @Query("SELECT t FROM Tour t WHERE t.isActive = true AND " +
           "t.pricePerPerson >= :minPrice AND t.pricePerPerson <= :maxPrice")
    Page<Tour> findActiveByPriceRangeOrderByCreatedAtDesc(
        @Param("minPrice") BigDecimal minPrice, 
        @Param("maxPrice") BigDecimal maxPrice, 
        Pageable pageable
    );
    
    // Find tours by duration range
    @Query("SELECT t FROM Tour t WHERE t.isActive = true AND " +
           "t.durationHours >= :minDuration AND t.durationHours <= :maxDuration")
    Page<Tour> findActiveByDurationRangeOrderByCreatedAtDesc(
        @Param("minDuration") Integer minDuration, 
        @Param("maxDuration") Integer maxDuration, 
        Pageable pageable
    );
    
    // Find tours with filters
    @Query("SELECT t FROM Tour t WHERE t.isActive = true " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:minPrice IS NULL OR t.pricePerPerson >= :minPrice) " +
           "AND (:maxPrice IS NULL OR t.pricePerPerson <= :maxPrice) " +
           "AND (:minDuration IS NULL OR t.durationHours >= :minDuration) " +
           "AND (:maxDuration IS NULL OR t.durationHours <= :maxDuration) " +
           "AND (:searchTerm IS NULL OR " +
           "     LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "     LOWER(t.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Tour> findActiveToursWithFilters(
        @Param("category") TourCategory category,
        @Param("minPrice") BigDecimal minPrice,
        @Param("maxPrice") BigDecimal maxPrice,
        @Param("minDuration") Integer minDuration,
        @Param("maxDuration") Integer maxDuration,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );
    
    // Find active tours with their images
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN FETCH t.images WHERE t.isActive = true")
    Page<Tour> findActiveToursWithImages(Pageable pageable);
    
    // Find tour by id with images
    @Query("SELECT t FROM Tour t LEFT JOIN FETCH t.images WHERE t.id = :id AND t.isActive = true")
    Optional<Tour> findActiveByIdWithImages(@Param("id") Long id);
    
    // Find tours by guide with images
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN FETCH t.images WHERE t.guide.id = :guideId AND t.isActive = true")
    List<Tour> findActiveByGuideIdWithImages(@Param("guideId") Long guideId);
    
    // Get tour count by guide
    @Query("SELECT COUNT(t) FROM Tour t WHERE t.guide.id = :guideId AND t.isActive = true")
    Long countActiveByGuideId(@Param("guideId") Long guideId);
    
    // Get tour count by category
    @Query("SELECT COUNT(t) FROM Tour t WHERE t.category = :category AND t.isActive = true")
    Long countActiveByCategory(@Param("category") TourCategory category);
    
    // Find tours by instant booking availability
    @Query("SELECT t FROM Tour t WHERE t.isActive = true AND t.instantBooking = :instantBooking")
    Page<Tour> findActiveByInstantBookingOrderByCreatedAtDesc(
        @Param("instantBooking") Boolean instantBooking, 
        Pageable pageable
    );
    
    // Get popular tours (most booked)
    @Query("SELECT t FROM Tour t LEFT JOIN Booking b ON t.id = b.tour.id " +
           "WHERE t.isActive = true " +
           "GROUP BY t.id " +
           "ORDER BY COUNT(b.id) DESC")
    Page<Tour> findMostPopularActiveTours(Pageable pageable);
    
    // Find all tours including inactive (for admin)
    @Query("SELECT t FROM Tour t ORDER BY t.createdAt DESC")
    Page<Tour> findAllOrderByCreatedAtDesc(Pageable pageable);
    
    // Find tours by guide including inactive (for guide management)
    @Query("SELECT t FROM Tour t WHERE t.guide.id = :guideId ORDER BY t.createdAt DESC")
    List<Tour> findAllByGuideIdOrderByCreatedAtDesc(@Param("guideId") Long guideId);
    
    // Find active tours from verified guides
    @Query("SELECT t FROM Tour t WHERE t.isActive = true AND t.guide.verificationStatus = 'VERIFIED' ORDER BY t.createdAt DESC")
    Page<Tour> findActiveToursFromVerifiedGuides(Pageable pageable);
    
    // Find active tours from verified guides with images
    @Query("SELECT DISTINCT t FROM Tour t LEFT JOIN FETCH t.images WHERE t.isActive = true AND t.guide.verificationStatus = 'VERIFIED' ORDER BY t.createdAt DESC")
    Page<Tour> findActiveToursFromVerifiedGuidesWithImages(Pageable pageable);
}


