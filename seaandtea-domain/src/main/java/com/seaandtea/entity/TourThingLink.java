package com.seaandtea.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tour_thing_links")
@Getter
@Setter
@NoArgsConstructor
public class TourThingLink {

    @EmbeddedId
    private TourThingLinkId id;

    public TourThingLink(Long tourId, ThingType thingType, Long thingId) {
        this.id = new TourThingLinkId(tourId, thingType, thingId);
    }
}
