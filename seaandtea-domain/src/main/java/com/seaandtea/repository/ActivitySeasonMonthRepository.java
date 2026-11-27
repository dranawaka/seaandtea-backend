package com.seaandtea.repository;

import com.seaandtea.entity.ActivitySeasonMonth;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivitySeasonMonthRepository extends JpaRepository<ActivitySeasonMonth, Long> {

    Optional<ActivitySeasonMonth> findByActivitySlugAndMonth(String slug, Integer month);
}
