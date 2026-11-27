package com.seaandtea.thingstodo;

import com.seaandtea.entity.Activity;
import com.seaandtea.entity.ThingStatus;
import com.seaandtea.repository.ActivityRepository;
import com.seaandtea.repository.ActivitySeasonMonthRepository;
import com.seaandtea.repository.DestinationRepository;
import com.seaandtea.repository.EventRepository;
import com.seaandtea.repository.RestaurantRepository;
import com.seaandtea.service.ThingsToDoSeeder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "things-to-do.seed.enabled=true")
class ThingsToDoSeederTest {

    @Autowired
    private ThingsToDoSeeder seeder;

    @Autowired
    private DestinationRepository destinations;

    @Autowired
    private ActivityRepository activities;

    @Autowired
    private EventRepository events;

    @Autowired
    private RestaurantRepository restaurants;

    @Autowired
    private ActivitySeasonMonthRepository seasonMonths;

    @Test
    void seedIsIdempotentAndLeavesAdminEdits() throws Exception {
        assertEquals(25, destinations.count());
        assertEquals(15, activities.count());
        assertEquals(13, events.count());
        assertEquals(10, restaurants.count());

        seeder.seed();

        assertEquals(25, destinations.count());
        assertEquals(15, activities.count());
        assertEquals(ThingStatus.DRAFT, activities.findBySlug("tea-estate-visits").orElseThrow().getStatus());
        assertEquals(ThingStatus.PUBLISHED, activities.findBySlug("arugam-bay-surfing").orElseThrow().getStatus());
        assertTrue(seasonMonths.findByActivitySlugAndMonth("arugam-bay-surfing", 7).orElseThrow().isPeak());
        assertFalse(seasonMonths.findByActivitySlugAndMonth("weligama-surfing", 7).isPresent());

        Activity surfing = activities.findBySlug("arugam-bay-surfing").orElseThrow();
        surfing.setSummary("Edited by an admin");
        surfing.setPreserveTimestamps(false);
        activities.saveAndFlush(surfing);

        seeder.seed();

        assertEquals("Edited by an admin", activities.findBySlug("arugam-bay-surfing").orElseThrow().getSummary());
        assertEquals(15, activities.count());
    }
}
