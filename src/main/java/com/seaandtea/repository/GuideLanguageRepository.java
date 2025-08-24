package com.seaandtea.repository;

import com.seaandtea.entity.GuideLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuideLanguageRepository extends JpaRepository<GuideLanguage, Long> {
    
    /**
     * Find all languages for a specific guide
     */
    List<GuideLanguage> findByGuideId(Long guideId);
    
    /**
     * Delete all languages for a specific guide
     */
    void deleteByGuideId(Long guideId);
}

