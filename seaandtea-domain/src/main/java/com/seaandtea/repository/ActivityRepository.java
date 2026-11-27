package com.seaandtea.repository;

import com.seaandtea.entity.Activity;
import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ThingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    Optional<Activity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Activity> findByStatus(ThingStatus status);

    long countByStatusAndCategory(ThingStatus status, ActivityCategory category);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Activity a set a.seasonalAlternative = null where a.seasonalAlternative.id = :id")
    void detachAlternative(@Param("id") Long id);
}
