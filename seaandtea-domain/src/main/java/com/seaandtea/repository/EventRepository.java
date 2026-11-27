package com.seaandtea.repository;

import com.seaandtea.entity.Event;
import com.seaandtea.entity.ThingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, Long id);

    List<Event> findByStatus(ThingStatus status);
}
