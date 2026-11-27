package com.seaandtea.repository;

import com.seaandtea.entity.EventOccurrence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventOccurrenceRepository extends JpaRepository<EventOccurrence, Long> {

    Optional<EventOccurrence> findByEventIdAndYear(Long eventId, Integer year);
}
