package com.seaandtea.repository;

import com.seaandtea.entity.Guide;
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
public interface GuideRepository extends JpaRepository<Guide, Long> {
    
    @Query("SELECT g FROM Guide g WHERE g.user.id = :userId")
    Optional<Guide> findByUserId(Long userId);
    
    @Query("SELECT g FROM Guide g WHERE g.verificationStatus = 'VERIFIED' AND g.isAvailable = true")
    Page<Guide> findAvailableVerifiedGuides(Pageable pageable);
    
    @Query("SELECT g FROM Guide g WHERE g.verificationStatus = 'VERIFIED' AND g.isAvailable = true " +
           "AND (:specialty IS NULL OR EXISTS (SELECT s FROM GuideSpecialty s WHERE s.guide = g AND s.specialty = :specialty)) " +
           "AND (:language IS NULL OR EXISTS (SELECT l FROM GuideLanguage l WHERE l.guide = g AND l.language = :language)) " +
           "AND (:minPrice IS NULL OR g.hourlyRate >= :minPrice) " +
           "AND (:maxPrice IS NULL OR g.hourlyRate <= :maxPrice)")
    Page<Guide> searchGuides(@Param("specialty") String specialty,
                             @Param("language") String language,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             Pageable pageable);
}

