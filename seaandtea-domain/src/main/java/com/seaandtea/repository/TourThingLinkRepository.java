package com.seaandtea.repository;

import com.seaandtea.entity.ThingType;
import com.seaandtea.entity.TourThingLink;
import com.seaandtea.entity.TourThingLinkId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TourThingLinkRepository extends JpaRepository<TourThingLink, TourThingLinkId> {

    List<TourThingLink> findByIdTourId(Long tourId);

    List<TourThingLink> findByIdThingTypeAndIdThingId(ThingType thingType, Long thingId);

    void deleteByIdTourId(Long tourId);
}
