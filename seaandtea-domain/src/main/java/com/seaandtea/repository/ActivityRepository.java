package com.seaandtea.repository;

import com.seaandtea.entity.Activity;
import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ThingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ActivityRepository extends JpaRepository<Activity, Long>, JpaSpecificationExecutor<Activity> {

    Optional<Activity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Activity> findByStatus(ThingStatus status);

    long countByStatusAndCategory(ThingStatus status, ActivityCategory category);
}
