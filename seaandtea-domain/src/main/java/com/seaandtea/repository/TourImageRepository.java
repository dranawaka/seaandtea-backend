package com.seaandtea.repository;

import com.seaandtea.entity.TourImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourImageRepository extends JpaRepository<TourImage, Long> {
    
    // Find all images for a tour
    @Query("SELECT ti FROM TourImage ti WHERE ti.tour.id = :tourId ORDER BY ti.isPrimary DESC, ti.createdAt ASC")
    List<TourImage> findByTourIdOrderByPrimaryAndCreatedAt(@Param("tourId") Long tourId);
    
    // Find primary image for a tour
    @Query("SELECT ti FROM TourImage ti WHERE ti.tour.id = :tourId AND ti.isPrimary = true")
    Optional<TourImage> findPrimaryByTourId(@Param("tourId") Long tourId);
    
    // Count images for a tour
    @Query("SELECT COUNT(ti) FROM TourImage ti WHERE ti.tour.id = :tourId")
    Long countByTourId(@Param("tourId") Long tourId);
    
    // Set all images for a tour as non-primary
    @Modifying
    @Query("UPDATE TourImage ti SET ti.isPrimary = false WHERE ti.tour.id = :tourId")
    void setAllNonPrimaryForTour(@Param("tourId") Long tourId);
    
    // Delete images by tour id
    @Modifying
    @Query("DELETE FROM TourImage ti WHERE ti.tour.id = :tourId")
    void deleteByTourId(@Param("tourId") Long tourId);
    
    // Find non-primary images for a tour
    @Query("SELECT ti FROM TourImage ti WHERE ti.tour.id = :tourId AND ti.isPrimary = false ORDER BY ti.createdAt ASC")
    List<TourImage> findNonPrimaryByTourId(@Param("tourId") Long tourId);
    
    // Check if image exists for tour
    @Query("SELECT COUNT(ti) > 0 FROM TourImage ti WHERE ti.tour.id = :tourId AND ti.imageUrl = :imageUrl")
    boolean existsByTourIdAndImageUrl(@Param("tourId") Long tourId, @Param("imageUrl") String imageUrl);
}


