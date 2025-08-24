package com.seaandtea.repository;

import com.seaandtea.entity.GuideSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideSpecialtyRepository extends JpaRepository<GuideSpecialty, Long> {
    
    /**
     * Find all specialties for a specific guide
     */
    List<GuideSpecialty> findByGuideId(Long guideId);
    
    /**
     * Delete all specialties for a specific guide
     */
    void deleteByGuideId(Long guideId);
}

