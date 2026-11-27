package com.seaandtea.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaandtea.entity.Activity;
import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ActivityRegion;
import com.seaandtea.entity.ActivitySeasonMonth;
import com.seaandtea.entity.ActivitySource;
import com.seaandtea.entity.Cuisine;
import com.seaandtea.entity.DateStatus;
import com.seaandtea.entity.Destination;
import com.seaandtea.entity.DestinationRegion;
import com.seaandtea.entity.Dietary;
import com.seaandtea.entity.Difficulty;
import com.seaandtea.entity.Event;
import com.seaandtea.entity.EventOccurrence;
import com.seaandtea.entity.EventSource;
import com.seaandtea.entity.EventType;
import com.seaandtea.entity.PriceTier;
import com.seaandtea.entity.RecurrenceType;
import com.seaandtea.entity.Restaurant;
import com.seaandtea.entity.RestaurantSource;
import com.seaandtea.entity.SourceKind;
import com.seaandtea.entity.ThingStatus;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.Tradition;
import com.seaandtea.repository.ActivityRepository;
import com.seaandtea.repository.DestinationRepository;
import com.seaandtea.repository.EventRepository;
import com.seaandtea.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "things-to-do.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class ThingsToDoSeeder implements ApplicationRunner {

    static final LocalDateTime ACCESSED_AT = LocalDateTime.of(2026, 11, 26, 0, 0);

    private final ObjectMapper objectMapper;
    private final DestinationRepository destinationRepository;
    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final RestaurantRepository restaurantRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        seed();
    }

    @Transactional
    public void seed() throws Exception {
        JsonNode root = objectMapper.readTree(new ClassPathResource("seed/things-to-do-seed.json").getInputStream());
        Map<String, Destination> destinations = new HashMap<>();
        for (JsonNode node : root.withArray("destinations")) {
            Destination destination = upsertDestination(node);
            destinations.put(destination.getSlug(), destination);
        }
        destinationRepository.flush();

        Map<String, Activity> activities = new HashMap<>();
        for (JsonNode node : root.withArray("activities")) {
            Activity activity = upsertActivity(node, destinations);
            activities.put(activity.getSlug(), activity);
        }
        activityRepository.flush();
        for (JsonNode node : root.withArray("activities")) {
            linkAlternative(node, activities);
        }

        for (JsonNode node : root.withArray("events")) {
            upsertEvent(node, destinations);
        }
        for (JsonNode node : root.withArray("restaurants")) {
            upsertRestaurant(node, destinations);
        }
        log.info("Things to do seed finished: {} destinations, {} activities, {} events, {} restaurants",
                root.withArray("destinations").size(),
                root.withArray("activities").size(),
                root.withArray("events").size(),
                root.withArray("restaurants").size());
    }

    private Destination upsertDestination(JsonNode node) {
        String slug = node.get("slug").asText();
        Destination destination = destinationRepository.findBySlug(slug).orElseGet(Destination::new);
        if (destination.getId() != null && destination.wasEditedAfterCreate()) {
            return destination;
        }
        destination.setSlug(slug);
        destination.setName(node.get("name").asText());
        destination.setRegion(DestinationRegion.valueOf(node.get("region").asText()));
        destination.setActive(true);
        keepSeedTimestamps(destination);
        return destinationRepository.save(destination);
    }

    private Activity upsertActivity(JsonNode node, Map<String, Destination> destinations) {
        String slug = node.get("slug").asText();
        Activity activity = activityRepository.findBySlug(slug).orElseGet(Activity::new);
        if (activity.getId() != null && activity.wasEditedAfterCreate()) {
            return activity;
        }
        activity.setSlug(slug);
        activity.setTitle(node.get("title").asText());
        activity.setCategory(ActivityCategory.valueOf(node.get("category").asText()));
        activity.setRegion(ActivityRegion.valueOf(node.get("region").asText()));
        activity.setDestination(requireDestination(destinations, node.get("destinationSlug").asText()));
        activity.setYearRound(node.path("yearRound").asBoolean(false));
        activity.setSeasonNote(text(node, "seasonNote"));
        activity.setDifficulty(enumOrNull(node, "difficulty", Difficulty.class));
        activity.setSummary(text(node, "summary"));
        activity.setResponsibleTravelNote(text(node, "responsibleTravelNote"));
        activity.setStatus(ThingStatus.valueOf(node.get("status").asText()));
        activity.setNeedsVerification(text(node, "needsVerification"));
        activity.getTourCategories().clear();
        activity.getNearbyDestinations().clear();
        activity.getSeasonMonths().clear();
        activity.getSources().clear();
        if (activity.getId() != null) {
            activityRepository.saveAndFlush(activity);
        }
        for (JsonNode category : node.withArray("tourCategories")) {
            activity.getTourCategories().add(Tour.TourCategory.valueOf(category.asText()));
        }
        for (JsonNode nearby : node.withArray("nearbyDestinationSlugs")) {
            activity.getNearbyDestinations().add(requireDestination(destinations, nearby.asText()));
        }
        addSeason(activity, node);
        for (JsonNode source : node.withArray("sources")) {
            ActivitySource row = new ActivitySource();
            row.setActivity(activity);
            fillSource(row, source);
            activity.getSources().add(row);
        }
        keepSeedTimestamps(activity);
        return activityRepository.save(activity);
    }

    private void linkAlternative(JsonNode node, Map<String, Activity> activities) {
        Activity activity = activities.get(node.get("slug").asText());
        if (activity.wasEditedAfterCreate()) {
            return;
        }
        String alternativeSlug = text(node, "seasonalAlternativeSlug");
        activity.setSeasonalAlternative(alternativeSlug == null ? null : activities.get(alternativeSlug));
        activity.setPreserveTimestamps(true);
        if (activity.getCreatedAt() != null) {
            activity.setUpdatedAt(activity.getCreatedAt());
        }
        activityRepository.save(activity);
    }

    private void addSeason(Activity activity, JsonNode node) {
        Set<Integer> season = intSet(node, "seasonMonths");
        Set<Integer> peak = intSet(node, "peakMonths");
        Set<Integer> months = new HashSet<>();
        months.addAll(season);
        months.addAll(peak);
        for (Integer month : months) {
            ActivitySeasonMonth row = new ActivitySeasonMonth();
            row.setActivity(activity);
            row.setMonth(month);
            row.setInSeason(season.contains(month));
            row.setPeak(peak.contains(month));
            activity.getSeasonMonths().add(row);
        }
    }

    private Event upsertEvent(JsonNode node, Map<String, Destination> destinations) {
        String slug = node.get("slug").asText();
        Event event = eventRepository.findBySlug(slug).orElseGet(Event::new);
        if (event.getId() != null && event.wasEditedAfterCreate()) {
            return event;
        }
        event.setSlug(slug);
        event.setName(node.get("name").asText());
        event.setType(EventType.valueOf(node.get("type").asText()));
        event.setTradition(Tradition.valueOf(node.get("tradition").asText()));
        event.setRecurrenceType(RecurrenceType.valueOf(node.get("recurrenceType").asText()));
        event.setPoyaName(text(node, "poyaName"));
        event.setTypicalStartMonth(intOrNull(node, "typicalStartMonth"));
        event.setTypicalEndMonth(intOrNull(node, "typicalEndMonth"));
        event.setTypicalDurationDays(intOrNull(node, "typicalDurationDays"));
        event.setDestination(requireDestination(destinations, node.get("destinationSlug").asText()));
        event.setVenueName(text(node, "venueName"));
        event.setOfficialUrl(text(node, "officialUrl"));
        event.setFree(node.path("isFree").asBoolean(false));
        event.setSummary(text(node, "summary"));
        event.setStatus(ThingStatus.valueOf(node.get("status").asText()));
        event.setNeedsVerification(text(node, "needsVerification"));
        event.getOccurrences().clear();
        event.getSources().clear();
        if (event.getId() != null) {
            eventRepository.saveAndFlush(event);
        }
        for (JsonNode occurrence : node.withArray("occurrences")) {
            EventOccurrence row = new EventOccurrence();
            row.setEvent(event);
            row.setYear(occurrence.get("year").asInt());
            row.setStartDate(date(occurrence, "startDate"));
            row.setEndDate(date(occurrence, "endDate"));
            row.setDateStatus(DateStatus.valueOf(occurrence.get("dateStatus").asText()));
            row.setNotes(text(occurrence, "notes"));
            row.setStatusSourceUrl(text(occurrence, "statusSourceUrl"));
            row.setTicketUrl(text(occurrence, "ticketUrl"));
            row.setSourceCheckedAt(ACCESSED_AT);
            event.getOccurrences().add(row);
        }
        for (JsonNode source : node.withArray("sources")) {
            EventSource row = new EventSource();
            row.setEvent(event);
            fillSource(row, source);
            event.getSources().add(row);
        }
        keepSeedTimestamps(event);
        return eventRepository.save(event);
    }

    private Restaurant upsertRestaurant(JsonNode node, Map<String, Destination> destinations) {
        String slug = node.get("slug").asText();
        Restaurant restaurant = restaurantRepository.findBySlug(slug).orElseGet(Restaurant::new);
        if (restaurant.getId() != null && restaurant.wasEditedAfterCreate()) {
            return restaurant;
        }
        restaurant.setSlug(slug);
        restaurant.setName(node.get("name").asText());
        restaurant.setDestination(requireDestination(destinations, node.get("destinationSlug").asText()));
        restaurant.setNeighbourhood(text(node, "neighbourhood"));
        restaurant.setAddressLine(text(node, "addressLine"));
        restaurant.setOpeningHoursText(text(node, "openingHoursText"));
        restaurant.setPriceTier(enumOrNull(node, "priceTier", PriceTier.class));
        restaurant.setPriceNote(text(node, "priceNote"));
        restaurant.setPhone(text(node, "phone"));
        restaurant.setWebsiteUrl(text(node, "websiteUrl"));
        restaurant.setReservationsEmail(text(node, "reservationsEmail"));
        restaurant.setSummary(text(node, "summary"));
        restaurant.setClosedOnPoya(node.path("closedOnPoya").asBoolean(false));
        restaurant.setServesAlcohol(node.has("servesAlcohol") && !node.get("servesAlcohol").isNull()
                ? node.get("servesAlcohol").asBoolean()
                : null);
        restaurant.setStatus(ThingStatus.valueOf(node.get("status").asText()));
        restaurant.setNeedsVerification(text(node, "needsVerification"));
        restaurant.getCuisines().clear();
        restaurant.getDietary().clear();
        restaurant.getSources().clear();
        if (restaurant.getId() != null) {
            restaurantRepository.saveAndFlush(restaurant);
        }
        for (JsonNode cuisine : node.withArray("cuisines")) {
            restaurant.getCuisines().add(Cuisine.valueOf(cuisine.asText()));
        }
        for (JsonNode dietary : node.withArray("dietary")) {
            restaurant.getDietary().add(Dietary.valueOf(dietary.asText()));
        }
        for (JsonNode source : node.withArray("sources")) {
            RestaurantSource row = new RestaurantSource();
            row.setRestaurant(restaurant);
            fillSource(row, source);
            restaurant.getSources().add(row);
        }
        keepSeedTimestamps(restaurant);
        return restaurantRepository.save(restaurant);
    }

    private void fillSource(com.seaandtea.entity.ThingSourceBase row, JsonNode source) {
        row.setUrl(source.get("url").asText());
        row.setLabel(text(source, "label"));
        row.setKind(SourceKind.valueOf(source.get("kind").asText()));
        row.setAccessedAt(ACCESSED_AT);
    }

    private void keepSeedTimestamps(com.seaandtea.entity.ThingBase thing) {
        if (thing.getId() == null) {
            return;
        }
        thing.setPreserveTimestamps(true);
        thing.setUpdatedAt(thing.getCreatedAt());
    }

    private void keepSeedTimestamps(Destination destination) {
        if (destination.getId() == null) {
            return;
        }
        destination.setPreserveTimestamps(true);
        destination.setUpdatedAt(destination.getCreatedAt());
    }

    private Destination requireDestination(Map<String, Destination> destinations, String slug) {
        Destination destination = destinations.get(slug);
        if (destination == null) {
            throw new IllegalStateException("Seed destination not found: " + slug);
        }
        return destination;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text.isBlank() ? null : text;
    }

    private static Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asInt();
    }

    private static LocalDate date(JsonNode node, String field) {
        String value = text(node, field);
        return value == null ? null : LocalDate.parse(value);
    }

    private static Set<Integer> intSet(JsonNode node, String field) {
        Set<Integer> values = new HashSet<>();
        for (JsonNode item : node.withArray(field)) {
            values.add(item.asInt());
        }
        return values;
    }

    private static <E extends Enum<E>> E enumOrNull(JsonNode node, String field, Class<E> type) {
        String value = text(node, field);
        return value == null ? null : Enum.valueOf(type, value);
    }
}
