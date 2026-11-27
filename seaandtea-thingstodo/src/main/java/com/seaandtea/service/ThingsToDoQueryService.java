package com.seaandtea.service;

import com.seaandtea.dto.ThingDtos.ActivityCard;
import com.seaandtea.dto.ThingDtos.ActivityDetail;
import com.seaandtea.dto.ThingDtos.DestinationBundle;
import com.seaandtea.dto.ThingDtos.DestinationCard;
import com.seaandtea.dto.ThingDtos.EventCard;
import com.seaandtea.dto.ThingDtos.EventDetail;
import com.seaandtea.dto.ThingDtos.GalleryImage;
import com.seaandtea.dto.ThingDtos.OccurrenceCard;
import com.seaandtea.dto.ThingDtos.OccurrenceDetail;
import com.seaandtea.dto.ThingDtos.Ref;
import com.seaandtea.dto.ThingDtos.RestaurantCard;
import com.seaandtea.dto.ThingDtos.RestaurantDetail;
import com.seaandtea.dto.ThingDtos.SitemapEntry;
import com.seaandtea.dto.ThingDtos.SourceView;
import com.seaandtea.dto.ThingDtos.Summary;
import com.seaandtea.dto.ThingDtos.ThingTours;
import com.seaandtea.entity.Activity;
import com.seaandtea.entity.ActivityCategory;
import com.seaandtea.entity.ActivityImage;
import com.seaandtea.entity.ActivityRegion;
import com.seaandtea.entity.Cuisine;
import com.seaandtea.entity.Destination;
import com.seaandtea.entity.Dietary;
import com.seaandtea.entity.Difficulty;
import com.seaandtea.entity.Event;
import com.seaandtea.entity.EventImage;
import com.seaandtea.entity.EventOccurrence;
import com.seaandtea.entity.EventType;
import com.seaandtea.entity.PriceTier;
import com.seaandtea.entity.Restaurant;
import com.seaandtea.entity.RestaurantImage;
import com.seaandtea.entity.ThingImageBase;
import com.seaandtea.entity.ThingSourceBase;
import com.seaandtea.entity.ThingStatus;
import com.seaandtea.entity.ThingType;
import com.seaandtea.entity.Tour;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.ActivityRepository;
import com.seaandtea.repository.DestinationRepository;
import com.seaandtea.repository.EventRepository;
import com.seaandtea.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThingsToDoQueryService {

    static final ZoneId COLOMBO = ZoneId.of("Asia/Colombo");

    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final RestaurantRepository restaurantRepository;
    private final DestinationRepository destinationRepository;
    private final ThingTourLinkService linkService;

    public int month(Integer requested) {
        int month = requested == null ? LocalDate.now(COLOMBO).getMonthValue() : requested;
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        return month;
    }

    public Summary summary(Integer month, String destination) {
        int resolved = month(month);
        List<Tour> tours = linkService.activeTours();
        List<ActivityCard> inSeason = publishedActivities().stream()
                .filter(activity -> destinationMatches(activity.getDestination(), destination))
                .filter(activity -> activity.inSeasonDuring(resolved))
                .sorted(activityOrder(resolved))
                .limit(8)
                .map(activity -> activityCard(activity, resolved, tours))
                .toList();
        List<EventCard> upcoming = publishedEvents().stream()
                .filter(event -> destinationMatches(event.getDestination(), destination))
                .sorted(eventOrder())
                .limit(6)
                .map(this::eventCard)
                .toList();
        List<RestaurantCard> featured = publishedRestaurants().stream()
                .filter(restaurant -> destinationMatches(restaurant.getDestination(), destination))
                .sorted(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER))
                .limit(4)
                .map(this::restaurantCard)
                .toList();
        Map<ActivityCategory, Long> counts = new EnumMap<>(ActivityCategory.class);
        for (ActivityCategory category : ActivityCategory.values()) {
            long count = publishedActivities().stream()
                    .filter(activity -> activity.getCategory() == category)
                    .filter(activity -> destinationMatches(activity.getDestination(), destination))
                    .count();
            counts.put(category, count);
        }
        return Summary.builder()
                .month(resolved)
                .inSeasonActivities(inSeason)
                .upcomingEvents(upcoming)
                .featuredRestaurants(featured)
                .categoryCounts(counts)
                .build();
    }

    public Page<ActivityCard> activities(String q, List<String> destinations, ActivityCategory category,
                                         ActivityRegion region, Difficulty difficulty, Integer month,
                                         Boolean inSeason, String sort, int page, int size) {
        int resolved = month(month);
        if (sort != null && !sort.isBlank() && !sort.equals("inSeason") && !sort.equals("title")) {
            throw new IllegalArgumentException("sort must be inSeason or title");
        }
        List<Tour> tours = linkService.activeTours();
        Comparator<Activity> order = "title".equals(sort)
                ? Comparator.comparing(Activity::getTitle, String.CASE_INSENSITIVE_ORDER)
                : activityOrder(resolved);
        List<ActivityCard> filtered = publishedActivities().stream()
                .filter(activity -> contains(q, activity.getTitle(), activity.getSummary()))
                .filter(activity -> slugIn(activity.getDestination(), destinations))
                .filter(activity -> category == null || activity.getCategory() == category)
                .filter(activity -> region == null || activity.getRegion() == region)
                .filter(activity -> difficulty == null || activity.getDifficulty() == difficulty)
                .filter(activity -> !Boolean.TRUE.equals(inSeason) || activity.inSeasonDuring(resolved))
                .sorted(order)
                .map(activity -> activityCard(activity, resolved, tours))
                .toList();
        return page(filtered, page, size);
    }

    public ActivityDetail activity(String slug, Integer month) {
        int resolved = month(month == null ? null : month);
        Activity activity = publishedActivity(slug);
        List<Tour> matches = linkService.matchActivity(activity, linkService.activeTours());
        Ref alternative = publishedAlternative(activity);
        return ActivityDetail.builder()
                .slug(activity.getSlug())
                .title(activity.getTitle())
                .category(activity.getCategory())
                .region(activity.getRegion())
                .destination(ref(activity.getDestination()))
                .heroImageUrl(activity.getHeroImageUrl())
                .summary(activity.getSummary())
                .difficulty(activity.getDifficulty())
                .seasonMonths(activity.seasonMonthNumbers())
                .peakMonths(activity.peakMonthNumbers())
                .yearRound(activity.isYearRound())
                .inSeasonNow(activity.inSeasonDuring(resolved))
                .peakNow(activity.peakDuring(resolved))
                .seasonalAlternative(alternative)
                .linkedTourCount(matches.size())
                .fromPriceUsd(linkService.fromPriceUsd(matches))
                .description(activity.getDescription())
                .seasonNote(activity.getSeasonNote())
                .typicalDurationMinutes(activity.getTypicalDurationMinutes())
                .bestTimeOfDay(activity.getBestTimeOfDay())
                .responsibleTravelNote(activity.getResponsibleTravelNote())
                .officialUrl(activity.getOfficialUrl())
                .feesNote(activity.getFeesNote())
                .nearbyDestinations(activity.getNearbyDestinations().stream().map(this::ref).toList())
                .gallery(activity.getImages().stream().map(this::image).toList())
                .sources(activity.getSources().stream().map(this::source).toList())
                .lastVerifiedAt(activity.getLastVerifiedAt())
                .seoTitle(activity.getSeoTitle())
                .seoDescription(activity.getSeoDescription())
                .latitude(activity.getLatitude())
                .longitude(activity.getLongitude())
                .build();
    }

    public ThingTours activityTours(String slug, int size) {
        Activity activity = publishedActivity(slug);
        return linkService.toResponse(linkService.matchActivity(activity, linkService.activeTours()), size);
    }

    public Page<EventCard> events(String q, String destination, EventType type, Integer month,
                                   LocalDate from, LocalDate to, Boolean confirmedOnly, int page, int size) {
        Integer resolvedMonth = month == null ? null : month(month);
        List<EventCard> filtered = publishedEvents().stream()
                .filter(event -> contains(q, event.getName(), event.getSummary()))
                .filter(event -> destination == null || destination.isBlank()
                        || (event.getDestination() != null && destination.equalsIgnoreCase(event.getDestination().getSlug())))
                .filter(event -> type == null || event.getType() == type)
                .filter(event -> resolvedMonth == null || eventInMonth(event, resolvedMonth))
                .filter(event -> overlaps(event, from, to))
                .filter(event -> !Boolean.TRUE.equals(confirmedOnly) || hasConfirmed(event))
                .sorted(eventOrder())
                .map(this::eventCard)
                .toList();
        return page(filtered, page, size);
    }

    public EventDetail event(String slug) {
        Event event = publishedEvent(slug);
        return EventDetail.builder()
                .slug(event.getSlug())
                .name(event.getName())
                .type(event.getType())
                .tradition(event.getTradition())
                .destination(ref(event.getDestination()))
                .venueName(event.getVenueName())
                .heroImageUrl(event.getHeroImageUrl())
                .summary(event.getSummary())
                .recurrenceType(event.getRecurrenceType())
                .typicalStartMonth(event.getTypicalStartMonth())
                .typicalEndMonth(event.getTypicalEndMonth())
                .nextOccurrence(occurrenceCard(nextOccurrence(event)))
                .description(event.getDescription())
                .etiquetteNote(event.getEtiquetteNote())
                .crowdNote(event.getCrowdNote())
                .officialUrl(event.getOfficialUrl())
                .free(event.isFree())
                .occurrences(event.getOccurrences().stream().map(this::occurrenceDetail).toList())
                .gallery(event.getImages().stream().map(this::image).toList())
                .sources(event.getSources().stream().map(this::source).toList())
                .lastVerifiedAt(event.getLastVerifiedAt())
                .seoTitle(event.getSeoTitle())
                .seoDescription(event.getSeoDescription())
                .build();
    }

    public ThingTours eventTours(String slug, int size) {
        Event event = publishedEvent(slug);
        return linkService.toResponse(linkService.matchEvent(event, linkService.activeTours()), size);
    }

    public Page<RestaurantCard> restaurants(String q, String destination, Cuisine cuisine, PriceTier priceTier,
                                             Dietary dietary, String sort, int page, int size) {
        if (sort != null && !sort.isBlank() && !sort.equals("name") && !sort.equals("tier")) {
            throw new IllegalArgumentException("sort must be name or tier");
        }
        Comparator<Restaurant> order = "tier".equals(sort)
                ? Comparator.comparing((Restaurant restaurant) -> restaurant.getPriceTier() == null
                        ? Integer.MAX_VALUE : restaurant.getPriceTier().ordinal())
                .thenComparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER)
                : Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER);
        List<RestaurantCard> filtered = publishedRestaurants().stream()
                .filter(restaurant -> contains(q, restaurant.getName(), restaurant.getSummary(), restaurant.getNeighbourhood()))
                .filter(restaurant -> destination == null || destination.isBlank()
                        || (restaurant.getDestination() != null && destination.equalsIgnoreCase(restaurant.getDestination().getSlug())))
                .filter(restaurant -> cuisine == null || restaurant.getCuisines().contains(cuisine))
                .filter(restaurant -> priceTier == null || restaurant.getPriceTier() == priceTier)
                .filter(restaurant -> dietary == null || restaurant.getDietary().contains(dietary))
                .sorted(order)
                .map(this::restaurantCard)
                .toList();
        return page(filtered, page, size);
    }

    public RestaurantDetail restaurant(String slug) {
        Restaurant restaurant = publishedRestaurant(slug);
        return RestaurantDetail.builder()
                .slug(restaurant.getSlug())
                .name(restaurant.getName())
                .cuisines(sortedCuisines(restaurant))
                .priceTier(restaurant.getPriceTier())
                .priceNote(restaurant.getPriceNote())
                .destination(ref(restaurant.getDestination()))
                .neighbourhood(restaurant.getNeighbourhood())
                .heroImageUrl(restaurant.getHeroImageUrl())
                .summary(restaurant.getSummary())
                .dietary(restaurant.getDietary().stream().sorted(Comparator.comparing(Enum::name)).toList())
                .openingHoursText(restaurant.getOpeningHoursText())
                .addressLine(restaurant.getAddressLine())
                .phone(restaurant.getPhone())
                .whatsapp(restaurant.getWhatsapp())
                .websiteUrl(restaurant.getWebsiteUrl())
                .reservationsUrl(restaurant.getReservationsUrl())
                .closedOnPoya(restaurant.isClosedOnPoya())
                .servesAlcohol(restaurant.getServesAlcohol())
                .mapsUrl(restaurant.getMapsUrl())
                .googlePlaceId(restaurant.getGooglePlaceId())
                .guideTip(restaurant.getGuideTip())
                .gallery(restaurant.getImages().stream().map(this::image).toList())
                .sources(restaurant.getSources().stream().map(this::source).toList())
                .lastVerifiedAt(restaurant.getLastVerifiedAt())
                .latitude(restaurant.getLatitude())
                .longitude(restaurant.getLongitude())
                .build();
    }

    public ThingTours restaurantTours(String slug, int size) {
        Restaurant restaurant = publishedRestaurant(slug);
        return linkService.toResponse(linkService.matchRestaurant(restaurant, linkService.activeTours()), size);
    }

    public List<DestinationCard> destinations() {
        return destinationRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::destinationCard)
                .toList();
    }

    public DestinationBundle bundle(String slug) {
        Destination destination = destinationRepository.findBySlug(slug)
                .filter(Destination::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Destination", slug));
        int resolved = month(null);
        List<Tour> tours = linkService.activeTours();
        return DestinationBundle.builder()
                .destination(destinationCard(destination))
                .activities(publishedActivities().stream()
                        .filter(activity -> activity.getDestination() != null && slug.equals(activity.getDestination().getSlug()))
                        .sorted(activityOrder(resolved))
                        .map(activity -> activityCard(activity, resolved, tours))
                        .toList())
                .events(publishedEvents().stream()
                        .filter(event -> event.getDestination() != null && slug.equals(event.getDestination().getSlug()))
                        .sorted(eventOrder())
                        .map(this::eventCard)
                        .toList())
                .restaurants(publishedRestaurants().stream()
                        .filter(restaurant -> restaurant.getDestination() != null && slug.equals(restaurant.getDestination().getSlug()))
                        .sorted(Comparator.comparing(Restaurant::getName, String.CASE_INSENSITIVE_ORDER))
                        .map(this::restaurantCard)
                        .toList())
                .build();
    }

    public List<SitemapEntry> sitemap() {
        List<SitemapEntry> entries = new ArrayList<>();
        publishedActivities().forEach(activity -> entries.add(SitemapEntry.builder()
                .type(ThingType.ACTIVITY).slug(activity.getSlug()).updatedAt(activity.getUpdatedAt()).build()));
        publishedEvents().forEach(event -> entries.add(SitemapEntry.builder()
                .type(ThingType.EVENT).slug(event.getSlug()).updatedAt(event.getUpdatedAt()).build()));
        publishedRestaurants().forEach(restaurant -> entries.add(SitemapEntry.builder()
                .type(ThingType.RESTAURANT).slug(restaurant.getSlug()).updatedAt(restaurant.getUpdatedAt()).build()));
        return entries;
    }

    private ActivityCard activityCard(Activity activity, int month, List<Tour> tours) {
        List<Tour> matches = linkService.matchActivity(activity, tours);
        return ActivityCard.builder()
                .slug(activity.getSlug())
                .title(activity.getTitle())
                .category(activity.getCategory())
                .region(activity.getRegion())
                .destination(ref(activity.getDestination()))
                .heroImageUrl(activity.getHeroImageUrl())
                .summary(activity.getSummary())
                .difficulty(activity.getDifficulty())
                .seasonMonths(activity.seasonMonthNumbers())
                .peakMonths(activity.peakMonthNumbers())
                .yearRound(activity.isYearRound())
                .inSeasonNow(activity.inSeasonDuring(month))
                .peakNow(activity.peakDuring(month))
                .seasonalAlternative(publishedAlternative(activity))
                .linkedTourCount(matches.size())
                .fromPriceUsd(linkService.fromPriceUsd(matches))
                .build();
    }

    private Ref publishedAlternative(Activity activity) {
        Activity alternative = activity.getSeasonalAlternative();
        if (alternative == null || alternative.getStatus() != ThingStatus.PUBLISHED) {
            return null;
        }
        return Ref.builder().slug(alternative.getSlug()).name(alternative.getTitle()).build();
    }

    private EventCard eventCard(Event event) {
        return EventCard.builder()
                .slug(event.getSlug())
                .name(event.getName())
                .type(event.getType())
                .tradition(event.getTradition())
                .destination(ref(event.getDestination()))
                .venueName(event.getVenueName())
                .heroImageUrl(event.getHeroImageUrl())
                .summary(event.getSummary())
                .recurrenceType(event.getRecurrenceType())
                .typicalStartMonth(event.getTypicalStartMonth())
                .typicalEndMonth(event.getTypicalEndMonth())
                .nextOccurrence(occurrenceCard(nextOccurrence(event)))
                .build();
    }

    private RestaurantCard restaurantCard(Restaurant restaurant) {
        return RestaurantCard.builder()
                .slug(restaurant.getSlug())
                .name(restaurant.getName())
                .cuisines(sortedCuisines(restaurant))
                .priceTier(restaurant.getPriceTier())
                .priceNote(restaurant.getPriceNote())
                .destination(ref(restaurant.getDestination()))
                .neighbourhood(restaurant.getNeighbourhood())
                .heroImageUrl(restaurant.getHeroImageUrl())
                .summary(restaurant.getSummary())
                .dietary(restaurant.getDietary().stream().sorted(Comparator.comparing(Enum::name)).toList())
                .openingHoursText(restaurant.getOpeningHoursText())
                .build();
    }

    private List<Cuisine> sortedCuisines(Restaurant restaurant) {
        return restaurant.getCuisines().stream().sorted(Comparator.comparing(Enum::name)).toList();
    }

    private DestinationCard destinationCard(Destination destination) {
        return DestinationCard.builder()
                .slug(destination.getSlug())
                .name(destination.getName())
                .region(destination.getRegion())
                .heroImageUrl(destination.getHeroImageUrl())
                .build();
    }

    private Ref ref(Destination destination) {
        if (destination == null) {
            return null;
        }
        return Ref.builder().slug(destination.getSlug()).name(destination.getName()).build();
    }

    private SourceView source(ThingSourceBase source) {
        return SourceView.builder()
                .url(source.getUrl())
                .label(source.getLabel())
                .kind(source.getKind())
                .accessedAt(source.getAccessedAt())
                .build();
    }

    private GalleryImage image(ThingImageBase image) {
        return GalleryImage.builder()
                .url(image.getUrl())
                .altText(image.getAltText())
                .credit(image.getCredit())
                .licence(image.getLicence())
                .sourceUrl(image.getSourceUrl())
                .sortOrder(image.getSortOrder())
                .build();
    }

    private GalleryImage image(ActivityImage image) {
        return image((ThingImageBase) image);
    }

    private GalleryImage image(EventImage image) {
        return image((ThingImageBase) image);
    }

    private GalleryImage image(RestaurantImage image) {
        return image((ThingImageBase) image);
    }

    private OccurrenceCard occurrenceCard(EventOccurrence occurrence) {
        if (occurrence == null) {
            return null;
        }
        return OccurrenceCard.builder()
                .year(occurrence.getYear())
                .startDate(occurrence.getStartDate())
                .endDate(occurrence.getEndDate())
                .dateStatus(occurrence.getDateStatus())
                .build();
    }

    private OccurrenceDetail occurrenceDetail(EventOccurrence occurrence) {
        return OccurrenceDetail.builder()
                .id(occurrence.getId())
                .year(occurrence.getYear())
                .startDate(occurrence.getStartDate())
                .endDate(occurrence.getEndDate())
                .startTime(occurrence.getStartTime())
                .dateStatus(occurrence.getDateStatus())
                .statusSourceUrl(occurrence.getStatusSourceUrl())
                .sourceCheckedAt(occurrence.getSourceCheckedAt())
                .ticketUrl(occurrence.getTicketUrl())
                .notes(occurrence.getNotes())
                .build();
    }

    private EventOccurrence nextOccurrence(Event event) {
        LocalDate today = LocalDate.now(COLOMBO);
        return event.getOccurrences().stream()
                .filter(occurrence -> occurrence.getStartDate() != null && !occurrence.getStartDate().isBefore(today))
                .min(Comparator.comparing(EventOccurrence::getStartDate))
                .orElseGet(() -> event.getOccurrences().stream()
                        .max(Comparator.comparing(EventOccurrence::getYear))
                        .orElse(null));
    }

    private boolean eventInMonth(Event event, int month) {
        boolean typical = spans(event.getTypicalStartMonth(), event.getTypicalEndMonth(), month);
        boolean occurrence = event.getOccurrences().stream().anyMatch(item ->
                (item.getStartDate() != null && item.getStartDate().getMonthValue() == month)
                        || (item.getEndDate() != null && item.getEndDate().getMonthValue() == month));
        return typical || occurrence;
    }

    private boolean spans(Integer start, Integer end, int month) {
        if (start == null || end == null) {
            return false;
        }
        if (start <= end) {
            return month >= start && month <= end;
        }
        return month >= start || month <= end;
    }

    private boolean overlaps(Event event, LocalDate from, LocalDate to) {
        if (from == null && to == null) {
            return true;
        }
        if (event.getOccurrences().isEmpty()) {
            return false;
        }
        return event.getOccurrences().stream().anyMatch(occurrence -> {
            if (occurrence.getStartDate() == null) {
                return false;
            }
            LocalDate start = occurrence.getStartDate();
            LocalDate end = occurrence.getEndDate() == null ? start : occurrence.getEndDate();
            boolean afterFrom = from == null || !end.isBefore(from);
            boolean beforeTo = to == null || !start.isAfter(to);
            return afterFrom && beforeTo;
        });
    }

    private boolean hasConfirmed(Event event) {
        return event.getOccurrences().stream()
                .anyMatch(occurrence -> occurrence.getDateStatus() == com.seaandtea.entity.DateStatus.CONFIRMED);
    }

    private Comparator<Activity> activityOrder(int month) {
        return Comparator
                .comparing((Activity activity) -> activity.inSeasonDuring(month) ? 0 : 1)
                .thenComparing(activity -> activity.peakDuring(month) ? 0 : 1)
                .thenComparing(Activity::getTitle, String.CASE_INSENSITIVE_ORDER);
    }

    private Comparator<Event> eventOrder() {
        LocalDate today = LocalDate.now(COLOMBO);
        return Comparator
                .comparing((Event event) -> {
                    EventOccurrence next = nextOccurrence(event);
                    if (next == null || next.getStartDate() == null || next.getStartDate().isBefore(today)) {
                        return LocalDate.MAX;
                    }
                    return next.getStartDate();
                })
                .thenComparing(event -> event.getTypicalStartMonth() == null ? 99 : event.getTypicalStartMonth())
                .thenComparing(Event::getName, String.CASE_INSENSITIVE_ORDER);
    }

    private List<Activity> publishedActivities() {
        return activityRepository.findByStatus(ThingStatus.PUBLISHED);
    }

    private List<Event> publishedEvents() {
        return eventRepository.findByStatus(ThingStatus.PUBLISHED);
    }

    private List<Restaurant> publishedRestaurants() {
        return restaurantRepository.findByStatus(ThingStatus.PUBLISHED);
    }

    private Activity publishedActivity(String slug) {
        return activityRepository.findBySlug(slug)
                .filter(activity -> activity.getStatus() == ThingStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Activity", slug));
    }

    private Event publishedEvent(String slug) {
        return eventRepository.findBySlug(slug)
                .filter(event -> event.getStatus() == ThingStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Event", slug));
    }

    private Restaurant publishedRestaurant(String slug) {
        return restaurantRepository.findBySlug(slug)
                .filter(restaurant -> restaurant.getStatus() == ThingStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant", slug));
    }

    private boolean destinationMatches(Destination destination, String slug) {
        return slug == null || slug.isBlank()
                || (destination != null && slug.equalsIgnoreCase(destination.getSlug()));
    }

    private boolean slugIn(Destination destination, List<String> slugs) {
        if (slugs == null || slugs.isEmpty()) {
            return true;
        }
        if (destination == null) {
            return false;
        }
        return slugs.stream().anyMatch(slug -> slug.equalsIgnoreCase(destination.getSlug()));
    }

    private boolean contains(String q, String... fields) {
        if (q == null || q.isBlank()) {
            return true;
        }
        String needle = q.toLowerCase(Locale.ROOT);
        for (String field : fields) {
            if (field != null && field.toLowerCase(Locale.ROOT).contains(needle)) {
                return true;
            }
        }
        return false;
    }

    private <T> Page<T> page(List<T> items, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = size <= 0 ? 12 : Math.min(size, 50);
        int from = Math.min(safePage * safeSize, items.size());
        int to = Math.min(from + safeSize, items.size());
        return new PageImpl<>(items.subList(from, to), PageRequest.of(safePage, safeSize), items.size());
    }
}
