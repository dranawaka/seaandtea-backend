package com.seaandtea.repository;

import com.seaandtea.entity.Restaurant;
import com.seaandtea.entity.ThingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    Optional<Restaurant> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Restaurant> findByStatus(ThingStatus status);
}
