package com.seaandtea.service;

import com.seaandtea.dto.AdminDtos.ActivityInput;
import com.seaandtea.dto.AdminDtos.AdminRow;
import com.seaandtea.dto.AdminDtos.EventInput;
import com.seaandtea.dto.AdminDtos.OccurrenceInput;
import com.seaandtea.dto.AdminDtos.RestaurantInput;
import com.seaandtea.dto.AdminDtos.SourceInput;
import com.seaandtea.dto.AdminDtos.StatusInput;
import com.seaandtea.entity.Activity;
import com.seaandtea.entity.ActivityImage;
import com.seaandtea.entity.ActivitySeasonMonth;
import com.seaandtea.entity.ActivitySource;
import com.seaandtea.entity.Destination;
import com.seaandtea.entity.Event;
import com.seaandtea.entity.EventImage;
import com.seaandtea.entity.EventOccurrence;
import com.seaandtea.entity.EventSource;
import com.seaandtea.entity.Restaurant;
import com.seaandtea.entity.RestaurantImage;
import com.seaandtea.entity.RestaurantSource;
import com.seaandtea.entity.ThingBase;
import com.seaandtea.entity.ThingSourceBase;
import com.seaandtea.entity.ThingStatus;
import com.seaandtea.entity.ThingType;
import com.seaandtea.entity.User;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.ActivityRepository;
import com.seaandtea.repository.DestinationRepository;
import com.seaandtea.repository.EventOccurrenceRepository;
import com.seaandtea.repository.EventRepository;
import com.seaandtea.repository.RestaurantRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ThingsToDoAdminService {

    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final RestaurantRepository restaurantRepository;
    private final EventOccurrenceRepository occurrenceRepository;
    private final DestinationRepository destinationRepository;
    private final UserRepository userRepository;
    private final ThingsToDoDeployHook deployHook;

    public Page<AdminRow> list(ThingType type, ThingStatus status, String q, int page, int size) {
        List<AdminRow> rows = switch (type) {
            case ACTIVITY -> activityRepository.findAll().stream().map(this::row).toList();
            case EVENT -> eventRepository.findAll().stream().map(this::row).toList();
            case RESTAURANT -> restaurantRepository.findAll().stream().map(this::row).toList();
        };
        List<AdminRow> filtered = rows.stream()
                .filter(row -> status == null || row.getStatus() == status)
                .filter(row -> q == null || q.isBlank()
                        || contains(row.getTitle(), q) || contains(row.getSlug(), q))
                .sorted(Comparator.comparing(AdminRow::getTitle, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        return page(filtered, page, size);
    }

    public List<AdminRow> pending() {
        List<AdminRow> rows = new ArrayList<>();
        activityRepository.findByStatus(ThingStatus.PENDING_REVIEW).forEach(item -> rows.add(row(item)));
        eventRepository.findByStatus(ThingStatus.PENDING_REVIEW).forEach(item -> rows.add(row(item)));
        restaurantRepository.findByStatus(ThingStatus.PENDING_REVIEW).forEach(item -> rows.add(row(item)));
        return rows;
    }

    public Activity getActivity(Long id) {
        return activityRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Activity", id));
    }

    public Event getEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    public Restaurant getRestaurant(Long id) {
        return restaurantRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Restaurant", id));
    }

    public Activity createActivity(ActivityInput input) {
        Activity activity = new Activity();
        applyActivity(activity, input, null);
        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long id, ActivityInput input) {
        Activity activity = getActivity(id);
        applyActivity(activity, input, id);
        Activity saved = activityRepository.save(activity);
        deployHook.ping();
        return saved;
    }

    public void deleteActivity(Long id) {
        if (!activityRepository.existsById(id)) {
            throw new ResourceNotFoundException("Activity", id);
        }
        activityRepository.detachAlternative(id);
        activityRepository.deleteById(id);
    }

    public Event createEvent(EventInput input) {
        Event event = new Event();
        applyEvent(event, input, null);
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, EventInput input) {
        Event event = getEvent(id);
        applyEvent(event, input, id);
        Event saved = eventRepository.save(event);
        deployHook.ping();
        return saved;
    }

    public void deleteEvent(Long id) {
        Event event = getEvent(id);
        eventRepository.delete(event);
    }

    public Restaurant createRestaurant(RestaurantInput input) {
        Restaurant restaurant = new Restaurant();
        applyRestaurant(restaurant, input, null);
        return restaurantRepository.save(restaurant);
    }

    public Restaurant updateRestaurant(Long id, RestaurantInput input) {
        Restaurant restaurant = getRestaurant(id);
        applyRestaurant(restaurant, input, id);
        Restaurant saved = restaurantRepository.save(restaurant);
        deployHook.ping();
        return saved;
    }

    public void deleteRestaurant(Long id) {
        restaurantRepository.delete(getRestaurant(id));
    }

    public ThingBase setStatus(ThingType type, Long id, StatusInput input, Authentication authentication) {
        if (input == null || input.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
        ThingBase thing = switch (type) {
            case ACTIVITY -> getActivity(id);
            case EVENT -> getEvent(id);
            case RESTAURANT -> getRestaurant(id);
        };
        if (input.getStatus() == ThingStatus.PUBLISHED && sourceCount(thing) == 0) {
            throw new IllegalArgumentException("Add at least one source before publishing");
        }
        thing.setStatus(input.getStatus());
        if (input.getModerationNote() != null) {
            thing.setModerationNote(input.getModerationNote());
        }
        if (Boolean.TRUE.equals(input.getMarkVerified())) {
            thing.setLastVerifiedAt(LocalDateTime.now());
            userRepository.findByEmail(authentication.getName()).map(User::getId).ifPresent(thing::setVerifiedByUserId);
        }
        ThingBase saved = switch (type) {
            case ACTIVITY -> activityRepository.save((Activity) thing);
            case EVENT -> eventRepository.save((Event) thing);
            case RESTAURANT -> restaurantRepository.save((Restaurant) thing);
        };
        if (input.getStatus() == ThingStatus.PUBLISHED) {
            deployHook.ping();
        }
        return saved;
    }

    public EventOccurrence addOccurrence(Long eventId, OccurrenceInput input) {
        Event event = getEvent(eventId);
        validateOccurrence(input);
        if (event.getOccurrences().stream().anyMatch(item -> item.getYear().equals(input.getYear()))) {
            throw new IllegalStateException("An occurrence for " + input.getYear() + " already exists");
        }
        EventOccurrence occurrence = new EventOccurrence();
        occurrence.setEvent(event);
        copyOccurrence(occurrence, input);
        return occurrenceRepository.save(occurrence);
    }

    public EventOccurrence updateOccurrence(Long id, OccurrenceInput input) {
        EventOccurrence occurrence = occurrenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event occurrence", id));
        validateOccurrence(input);
        boolean duplicateYear = occurrence.getEvent().getOccurrences().stream()
                .anyMatch(item -> !item.getId().equals(id) && item.getYear().equals(input.getYear()));
        if (duplicateYear) {
            throw new IllegalStateException("An occurrence for " + input.getYear() + " already exists");
        }
        copyOccurrence(occurrence, input);
        return occurrenceRepository.save(occurrence);
    }

    public void deleteOccurrence(Long id) {
        if (!occurrenceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Event occurrence", id);
        }
        occurrenceRepository.deleteById(id);
    }

    public String attachImage(String type, Long id, String imageUrl, String altText) {
        ThingType thingType = parseType(type);
        switch (thingType) {
            case ACTIVITY -> {
                Activity activity = getActivity(id);
                ActivityImage image = new ActivityImage();
                image.setActivity(activity);
                image.setUrl(imageUrl);
                image.setAltText(altText);
                image.setSortOrder(activity.getImages().size());
                activity.getImages().add(image);
                if (activity.getHeroImageUrl() == null || activity.getHeroImageUrl().isBlank()) {
                    activity.setHeroImageUrl(imageUrl);
                }
                activityRepository.save(activity);
            }
            case EVENT -> {
                Event event = getEvent(id);
                EventImage image = new EventImage();
                image.setEvent(event);
                image.setUrl(imageUrl);
                image.setAltText(altText);
                image.setSortOrder(event.getImages().size());
                event.getImages().add(image);
                if (event.getHeroImageUrl() == null || event.getHeroImageUrl().isBlank()) {
                    event.setHeroImageUrl(imageUrl);
                }
                eventRepository.save(event);
            }
            case RESTAURANT -> {
                Restaurant restaurant = getRestaurant(id);
                RestaurantImage image = new RestaurantImage();
                image.setRestaurant(restaurant);
                image.setUrl(imageUrl);
                image.setAltText(altText);
                image.setSortOrder(restaurant.getImages().size());
                restaurant.getImages().add(image);
                if (restaurant.getHeroImageUrl() == null || restaurant.getHeroImageUrl().isBlank()) {
                    restaurant.setHeroImageUrl(imageUrl);
                }
                restaurantRepository.save(restaurant);
            }
            default -> throw new IllegalArgumentException("Unknown type");
        }
        return imageUrl;
    }

    public static ThingType parseType(String type) {
        return switch (type) {
            case "activities" -> ThingType.ACTIVITY;
            case "events" -> ThingType.EVENT;
            case "restaurants" -> ThingType.RESTAURANT;
            default -> throw new IllegalArgumentException("type must be activities, events, or restaurants");
        };
    }

    private void applyActivity(Activity activity, ActivityInput input, Long currentId) {
        require(input.getTitle(), "title");
        activity.setSlug(claimSlug(input.getSlug(), currentId, ThingType.ACTIVITY));
        activity.setTitle(input.getTitle().trim());
        activity.setSummary(summary(input.getSummary()));
        activity.setDescription(input.getDescription());
        activity.setDestination(destination(input.getDestinationSlug()));
        if (input.getCategory() == null || input.getRegion() == null) {
            throw new IllegalArgumentException("category and region are required");
        }
        activity.setCategory(input.getCategory());
        activity.setRegion(input.getRegion());
        activity.setYearRound(input.isYearRound());
        activity.setDifficulty(input.getDifficulty());
        activity.setSeasonNote(input.getSeasonNote());
        activity.setTypicalDurationMinutes(input.getTypicalDurationMinutes());
        activity.setBestTimeOfDay(input.getBestTimeOfDay());
        activity.setResponsibleTravelNote(input.getResponsibleTravelNote());
        activity.setOfficialUrl(url(input.getOfficialUrl(), "officialUrl"));
        activity.setFeesNote(input.getFeesNote());
        activity.setHeroImageUrl(input.getHeroImageUrl());
        activity.setLatitude(input.getLatitude());
        activity.setLongitude(input.getLongitude());
        activity.setSeoTitle(input.getSeoTitle());
        activity.setSeoDescription(input.getSeoDescription());
        activity.setNeedsVerification(input.getNeedsVerification());
        activity.getTourCategories().clear();
        if (input.getTourCategories() != null) {
            activity.getTourCategories().addAll(input.getTourCategories());
        }
        activity.getNearbyDestinations().clear();
        if (input.getNearbyDestinationSlugs() != null) {
            for (String slug : input.getNearbyDestinationSlugs()) {
                activity.getNearbyDestinations().add(destination(slug));
            }
        }
        if (input.getSeasonalAlternativeSlug() == null || input.getSeasonalAlternativeSlug().isBlank()) {
            activity.setSeasonalAlternative(null);
        } else {
            activity.setSeasonalAlternative(activityRepository.findBySlug(input.getSeasonalAlternativeSlug())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown seasonal alternative")));
        }
        replaceSeason(activity, input.getSeasonMonths(), input.getPeakMonths());
        replaceActivitySources(activity, input.getSources());
        ThingStatus status = input.getStatus() == null ? ThingStatus.DRAFT : input.getStatus();
        if (status == ThingStatus.PUBLISHED && activity.getSources().isEmpty()) {
            throw new IllegalArgumentException("Add at least one source before publishing");
        }
        activity.setStatus(status);
    }

    private void applyEvent(Event event, EventInput input, Long currentId) {
        require(input.getName(), "name");
        event.setSlug(claimSlug(input.getSlug(), currentId, ThingType.EVENT));
        event.setName(input.getName().trim());
        event.setSummary(summary(input.getSummary()));
        event.setDescription(input.getDescription());
        event.setDestination(destination(input.getDestinationSlug()));
        if (input.getType() == null || input.getTradition() == null || input.getRecurrenceType() == null) {
            throw new IllegalArgumentException("type, tradition, and recurrenceType are required");
        }
        event.setType(input.getType());
        event.setTradition(input.getTradition());
        event.setRecurrenceType(input.getRecurrenceType());
        event.setPoyaName(input.getPoyaName());
        event.setTypicalStartMonth(month(input.getTypicalStartMonth()));
        event.setTypicalEndMonth(month(input.getTypicalEndMonth()));
        event.setTypicalDurationDays(input.getTypicalDurationDays());
        event.setVenueName(input.getVenueName());
        event.setVenueAddress(input.getVenueAddress());
        event.setFree(input.isFree());
        event.setOfficialUrl(url(input.getOfficialUrl(), "officialUrl"));
        event.setEtiquetteNote(input.getEtiquetteNote());
        event.setCrowdNote(input.getCrowdNote());
        event.setHeroImageUrl(input.getHeroImageUrl());
        event.setLatitude(input.getLatitude());
        event.setLongitude(input.getLongitude());
        event.setSeoTitle(input.getSeoTitle());
        event.setSeoDescription(input.getSeoDescription());
        event.setNeedsVerification(input.getNeedsVerification());
        replaceEventSources(event, input.getSources());
        if (input.getOccurrences() != null) {
            event.getOccurrences().clear();
            if (event.getId() != null) {
                eventRepository.saveAndFlush(event);
            }
            Set<Integer> years = new HashSet<>();
            for (OccurrenceInput occurrence : input.getOccurrences()) {
                validateOccurrence(occurrence);
                if (!years.add(occurrence.getYear())) {
                    throw new IllegalStateException("An occurrence for " + occurrence.getYear() + " already exists");
                }
                EventOccurrence row = new EventOccurrence();
                row.setEvent(event);
                copyOccurrence(row, occurrence);
                event.getOccurrences().add(row);
            }
        }
        ThingStatus status = input.getStatus() == null ? ThingStatus.DRAFT : input.getStatus();
        if (status == ThingStatus.PUBLISHED && event.getSources().isEmpty()) {
            throw new IllegalArgumentException("Add at least one source before publishing");
        }
        event.setStatus(status);
    }

    private void applyRestaurant(Restaurant restaurant, RestaurantInput input, Long currentId) {
        require(input.getName(), "name");
        restaurant.setSlug(claimSlug(input.getSlug(), currentId, ThingType.RESTAURANT));
        restaurant.setName(input.getName().trim());
        restaurant.setSummary(summary(input.getSummary()));
        restaurant.setDescription(input.getDescription());
        restaurant.setDestination(destination(input.getDestinationSlug()));
        restaurant.getCuisines().clear();
        if (input.getCuisines() != null) {
            restaurant.getCuisines().addAll(input.getCuisines());
        }
        restaurant.setPriceTier(input.getPriceTier());
        restaurant.setPriceNote(input.getPriceNote());
        restaurant.setAddressLine(input.getAddressLine());
        restaurant.setNeighbourhood(input.getNeighbourhood());
        restaurant.setPhone(input.getPhone());
        restaurant.setWhatsapp(input.getWhatsapp());
        restaurant.setWebsiteUrl(url(input.getWebsiteUrl(), "websiteUrl"));
        restaurant.setReservationsUrl(url(input.getReservationsUrl(), "reservationsUrl"));
        restaurant.setReservationsEmail(input.getReservationsEmail());
        restaurant.setOpeningHoursText(input.getOpeningHoursText());
        restaurant.setOpeningHoursJson(input.getOpeningHoursJson());
        restaurant.setClosedOnPoya(input.isClosedOnPoya());
        restaurant.setServesAlcohol(input.getServesAlcohol());
        restaurant.getDietary().clear();
        if (input.getDietary() != null) {
            restaurant.getDietary().addAll(input.getDietary());
        }
        restaurant.setGoodFor(input.getGoodFor() == null ? new ArrayList<>() : new ArrayList<>(input.getGoodFor()));
        restaurant.setGooglePlaceId(input.getGooglePlaceId());
        restaurant.setMapsUrl(url(input.getMapsUrl(), "mapsUrl"));
        restaurant.setGuideTip(input.getGuideTip());
        restaurant.setHeroImageUrl(input.getHeroImageUrl());
        restaurant.setLatitude(input.getLatitude());
        restaurant.setLongitude(input.getLongitude());
        restaurant.setSeoTitle(input.getSeoTitle());
        restaurant.setSeoDescription(input.getSeoDescription());
        restaurant.setNeedsVerification(input.getNeedsVerification());
        replaceRestaurantSources(restaurant, input.getSources());
        ThingStatus status = input.getStatus() == null ? ThingStatus.DRAFT : input.getStatus();
        if (status == ThingStatus.PUBLISHED && restaurant.getSources().isEmpty()) {
            throw new IllegalArgumentException("Add at least one source before publishing");
        }
        restaurant.setStatus(status);
    }

    private void replaceSeason(Activity activity, List<Integer> seasonMonths, List<Integer> peakMonths) {
        Set<Integer> season = seasonMonths == null ? Set.of() : new HashSet<>(seasonMonths);
        Set<Integer> peak = peakMonths == null ? Set.of() : new HashSet<>(peakMonths);
        season.forEach(this::month);
        peak.forEach(this::month);
        activity.getSeasonMonths().clear();
        if (activity.getId() != null) {
            activityRepository.saveAndFlush(activity);
        }
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

    private void replaceActivitySources(Activity activity, List<SourceInput> sources) {
        activity.getSources().clear();
        if (activity.getId() != null) {
            activityRepository.saveAndFlush(activity);
        }
        if (sources == null) {
            return;
        }
        for (SourceInput source : sources) {
            ActivitySource row = new ActivitySource();
            row.setActivity(activity);
            copySource(row, source);
            activity.getSources().add(row);
        }
    }

    private void replaceEventSources(Event event, List<SourceInput> sources) {
        event.getSources().clear();
        if (event.getId() != null) {
            eventRepository.saveAndFlush(event);
        }
        if (sources == null) {
            return;
        }
        for (SourceInput source : sources) {
            EventSource row = new EventSource();
            row.setEvent(event);
            copySource(row, source);
            event.getSources().add(row);
        }
    }

    private void replaceRestaurantSources(Restaurant restaurant, List<SourceInput> sources) {
        restaurant.getSources().clear();
        if (restaurant.getId() != null) {
            restaurantRepository.saveAndFlush(restaurant);
        }
        if (sources == null) {
            return;
        }
        for (SourceInput source : sources) {
            RestaurantSource row = new RestaurantSource();
            row.setRestaurant(restaurant);
            copySource(row, source);
            restaurant.getSources().add(row);
        }
    }

    private void copySource(ThingSourceBase row, SourceInput source) {
        if (source == null || source.getUrl() == null || source.getKind() == null) {
            throw new IllegalArgumentException("Each source needs a url and a kind");
        }
        row.setUrl(url(source.getUrl(), "source url"));
        row.setLabel(source.getLabel());
        row.setKind(source.getKind());
        row.setAccessedAt(LocalDateTime.now());
    }

    private void copyOccurrence(EventOccurrence occurrence, OccurrenceInput input) {
        occurrence.setYear(input.getYear());
        occurrence.setStartDate(input.getStartDate());
        occurrence.setEndDate(input.getEndDate());
        occurrence.setStartTime(input.getStartTime());
        occurrence.setDateStatus(input.getDateStatus());
        occurrence.setStatusSourceUrl(url(input.getStatusSourceUrl(), "statusSourceUrl"));
        occurrence.setTicketUrl(url(input.getTicketUrl(), "ticketUrl"));
        occurrence.setNotes(input.getNotes());
        occurrence.setSourceCheckedAt(LocalDateTime.now());
    }

    private void validateOccurrence(OccurrenceInput input) {
        if (input == null || input.getYear() == null || input.getDateStatus() == null) {
            throw new IllegalArgumentException("An occurrence needs a year and a date status");
        }
        if (input.getStartDate() != null && input.getEndDate() != null && input.getStartDate().isAfter(input.getEndDate())) {
            throw new IllegalArgumentException("startDate must be on or before endDate");
        }
        if (input.getDateStatus() == com.seaandtea.entity.DateStatus.CONFIRMED && input.getStartDate() == null) {
            throw new IllegalArgumentException("A confirmed occurrence needs a start date");
        }
        if (input.getStartDate() == null
                && input.getDateStatus() != com.seaandtea.entity.DateStatus.EXPECTED
                && input.getDateStatus() != com.seaandtea.entity.DateStatus.TO_BE_ANNOUNCED) {
            throw new IllegalArgumentException("startDate may be empty only when the date is expected or still to be announced");
        }
    }

    private String claimSlug(String slug, Long currentId, ThingType type) {
        if (slug == null || !slug.matches("[a-z0-9-]{1,120}")) {
            throw new IllegalArgumentException("slug must be 1-120 characters of lowercase letters, numbers, and hyphens");
        }
        boolean taken = switch (type) {
            case ACTIVITY -> currentId == null
                    ? activityRepository.existsBySlug(slug)
                    : activityRepository.existsBySlugAndIdNot(slug, currentId);
            case EVENT -> currentId == null
                    ? eventRepository.existsBySlug(slug)
                    : eventRepository.existsBySlugAndIdNot(slug, currentId);
            case RESTAURANT -> currentId == null
                    ? restaurantRepository.existsBySlug(slug)
                    : restaurantRepository.existsBySlugAndIdNot(slug, currentId);
        };
        if (taken) {
            throw new IllegalStateException("Slug already exists: " + slug);
        }
        return slug;
    }

    private Destination destination(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("destinationSlug is required");
        }
        return destinationRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Unknown destination: " + slug));
    }

    private Integer month(Integer month) {
        if (month == null) {
            return null;
        }
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        return month;
    }

    private String summary(String summary) {
        if (summary != null && summary.length() > 300) {
            throw new IllegalArgumentException("summary must be 300 characters or fewer");
        }
        return summary;
    }

    private String url(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            throw new IllegalArgumentException(field + " must be an http(s) URL");
        }
        return value;
    }

    private void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private int sourceCount(ThingBase thing) {
        if (thing instanceof Activity activity) {
            return activity.getSources().size();
        }
        if (thing instanceof Event event) {
            return event.getSources().size();
        }
        if (thing instanceof Restaurant restaurant) {
            return restaurant.getSources().size();
        }
        return 0;
    }

    private AdminRow row(Activity activity) {
        return row(ThingType.ACTIVITY, activity, activity.getTitle(), activity.getDestination());
    }

    private AdminRow row(Event event) {
        return row(ThingType.EVENT, event, event.getName(), event.getDestination());
    }

    private AdminRow row(Restaurant restaurant) {
        return row(ThingType.RESTAURANT, restaurant, restaurant.getName(), restaurant.getDestination());
    }

    private AdminRow row(ThingType type, ThingBase thing, String title, Destination destination) {
        AdminRow row = new AdminRow();
        row.setId(thing.getId());
        row.setType(type);
        row.setSlug(thing.getSlug());
        row.setTitle(title);
        row.setDestination(destination == null ? null : destination.getName());
        row.setStatus(thing.getStatus());
        row.setNeedsVerification(thing.getNeedsVerification());
        row.setLastVerifiedAt(thing.getLastVerifiedAt());
        return row;
    }

    private boolean contains(String value, String q) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(q.toLowerCase(Locale.ROOT));
    }

    private <T> Page<T> page(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 12 : Math.min(size, 50);
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());
        return new PageImpl<>(items.subList(from, to), PageRequest.of(safePage, safeSize), items.size());
    }
}
