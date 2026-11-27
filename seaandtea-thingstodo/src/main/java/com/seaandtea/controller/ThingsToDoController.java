package com.seaandtea.controller;

import com.seaandtea.dto.ThingDtos.ActivityDetail;
import com.seaandtea.dto.ThingDtos.DestinationBundle;
import com.seaandtea.dto.ThingDtos.DestinationCard;
import com.seaandtea.dto.ThingDtos.EventDetail;
import com.seaandtea.dto.ThingDtos.RestaurantDetail;
import com.seaandtea.dto.ThingDtos.SitemapEntry;
import com.seaandtea.dto.ThingDtos.Summary;
import com.seaandtea.dto.ThingDtos.ThingTours;
import com.seaandtea.dto.ThingDtos.TourLinkRequest;
import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ActivityRegion;
import com.seaandtea.entity.Cuisine;
import com.seaandtea.entity.Dietary;
import com.seaandtea.entity.Difficulty;
import com.seaandtea.entity.EventType;
import com.seaandtea.entity.PriceTier;
import com.seaandtea.service.ThingTourLinkService;
import com.seaandtea.service.ThingsToDoQueryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Things to do", description = "Published activities, events, restaurants, and destinations")
public class ThingsToDoController {

    private final ThingsToDoQueryService queryService;
    private final ThingTourLinkService linkService;

    @GetMapping("/things-to-do/summary")
    public Summary summary(@RequestParam(required = false) Integer month,
                           @RequestParam(required = false) String destination) {
        return queryService.summary(month, destination);
    }

    @GetMapping("/things-to-do/sitemap")
    public List<SitemapEntry> sitemap() {
        return queryService.sitemap();
    }

    @GetMapping("/activities")
    public Page<com.seaandtea.dto.ThingDtos.ActivityCard> activities(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> destination,
            @RequestParam(required = false) ActivityCategory category,
            @RequestParam(required = false) ActivityRegion region,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Boolean inSeason,
            @RequestParam(defaultValue = "inSeason") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return queryService.activities(q, destination, category, region, difficulty, month, inSeason, sort, page, size);
    }

    @GetMapping("/activities/{slug}")
    public ActivityDetail activity(@PathVariable String slug, @RequestParam(required = false) Integer month) {
        return queryService.activity(slug, month);
    }

    @GetMapping("/activities/{slug}/tours")
    public ThingTours activityTours(@PathVariable String slug, @RequestParam(defaultValue = "6") int size) {
        return queryService.activityTours(slug, size);
    }

    @GetMapping("/events")
    public Page<com.seaandtea.dto.ThingDtos.EventCard> events(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) EventType type,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Boolean confirmedOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return queryService.events(q, destination, type, month, from, to, confirmedOnly, page, size);
    }

    @GetMapping("/events/{slug}")
    public EventDetail event(@PathVariable String slug) {
        return queryService.event(slug);
    }

    @GetMapping("/events/{slug}/tours")
    public ThingTours eventTours(@PathVariable String slug, @RequestParam(defaultValue = "6") int size) {
        return queryService.eventTours(slug, size);
    }

    @GetMapping("/restaurants")
    public Page<com.seaandtea.dto.ThingDtos.RestaurantCard> restaurants(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Cuisine cuisine,
            @RequestParam(required = false) PriceTier priceTier,
            @RequestParam(required = false) Dietary dietary,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return queryService.restaurants(q, destination, cuisine, priceTier, dietary, sort, page, size);
    }

    @GetMapping("/restaurants/{slug}")
    public RestaurantDetail restaurant(@PathVariable String slug) {
        return queryService.restaurant(slug);
    }

    @GetMapping("/restaurants/{slug}/tours")
    public ThingTours restaurantTours(@PathVariable String slug, @RequestParam(defaultValue = "6") int size) {
        return queryService.restaurantTours(slug, size);
    }

    @GetMapping("/destinations")
    public List<DestinationCard> destinations() {
        return queryService.destinations();
    }

    @GetMapping("/destinations/{slug}/things-to-do")
    public DestinationBundle destinationBundle(@PathVariable String slug) {
        return queryService.bundle(slug);
    }

    @PutMapping("/tours/{id}/things-to-do")
    public ResponseEntity<Void> replaceTourLinks(@PathVariable Long id,
                                                  @RequestBody List<TourLinkRequest> links,
                                                  Authentication authentication) {
        linkService.replaceTourLinks(id, links, authentication);
        return ResponseEntity.noContent().build();
    }
}
