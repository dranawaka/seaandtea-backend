package com.seaandtea.service;

import com.seaandtea.entity.Tour;
import com.seaandtea.repository.TourRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourMarketplaceBackfill {

    private final TourRepository tourRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void backfill() {
        try {
            int updated = 0;
            for (Tour tour : tourRepository.findAll()) {
                boolean changed = false;
                if (tour.getDestination() == null || tour.getDestination().isBlank()) {
                    tour.setDestination(Destinations.fromMeetingPoint(tour.getMeetingPoint()));
                    changed = true;
                }
                if (tour.getDepartureTime() == null) {
                    tour.setDepartureTime(LocalTime.of(9, 0));
                    changed = true;
                }
                if (changed) {
                    tourRepository.save(tour);
                    updated++;
                }
            }
            if (updated > 0) {
                log.info("Backfilled destination or departure time on {} tours", updated);
            }
        } catch (Exception ex) {
            log.error("Tour marketplace backfill failed; continuing without blocking startup", ex);
        }
    }
}
