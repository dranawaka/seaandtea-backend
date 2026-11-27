package com.seaandtea.repository;

import com.seaandtea.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findBySlug(String slug);

    List<Destination> findByActiveTrueOrderByNameAsc();
}
