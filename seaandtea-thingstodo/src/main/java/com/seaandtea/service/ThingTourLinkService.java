package com.seaandtea.service;

import com.seaandtea.dto.ThingDtos.GuideChip;
import com.seaandtea.dto.ThingDtos.ThingTours;
import com.seaandtea.dto.ThingDtos.TourLinkRequest;
import com.seaandtea.dto.TourListResponse;
import com.seaandtea.entity.Activity;
import com.seaandtea.entity.Destination;
import com.seaandtea.entity.Event;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.Restaurant;
import com.seaandtea.entity.ThingType;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.TourThingLink;
import com.seaandtea.entity.User;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.exception.UnauthorizedException;
import com.seaandtea.repository.ActivityRepository;
import com.seaandtea.repository.BookingRepository;
import com.seaandtea.repository.EventRepository;
import com.seaandtea.repository.RestaurantRepository;
import com.seaandtea.repository.ReviewRepository;
import com.seaandtea.repository.TourRepository;
import com.seaandtea.repository.TourThingLinkRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class ThingTourLinkService {

    private static final Pattern TEST_TITLE = Pattern.compile("^Test( \\d+)?$", Pattern.CASE_INSENSITIVE);

    private final TourRepository tourRepository;
    private final TourThingLinkRepository linkRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final EventRepository eventRepository;
    private final RestaurantRepository restaurantRepository;
    private final TourService tourService;

    @Transactional(readOnly = true)
    public List<Tour> activeTours() {
        return tourRepository.findActiveForLinking();
    }

    public List<Tour> matchActivity(Activity activity, List<Tour> activeTours) {
        Set<String> names = names(activity.getDestination(), activity.getNearbyDestinations());
        return match(ThingType.ACTIVITY, activity.getId(), activeTours, tour ->
                names.contains(lower(tour.getDestination()))
                        && activity.getTourCategories().contains(tour.getCategory()));
    }

    public List<Tour> matchEvent(Event event, List<Tour> activeTours) {
        String name = lower(event.getDestination() == null ? null : event.getDestination().getName());
        return match(ThingType.EVENT, event.getId(), activeTours, tour ->
                name.equals(lower(tour.getDestination()))
                        && tour.getCategory() == Tour.TourCategory.CULTURAL_TOURS);
    }

    public List<Tour> matchRestaurant(Restaurant restaurant, List<Tour> activeTours) {
        String name = lower(restaurant.getDestination() == null ? null : restaurant.getDestination().getName());
        return match(ThingType.RESTAURANT, restaurant.getId(), activeTours, tour ->
                name.equals(lower(tour.getDestination()))
                        && tour.getCategory() == Tour.TourCategory.FOOD_TOURS);
    }

    @Transactional(readOnly = true)
    public ThingTours toResponse(List<Tour> matches, int size) {
        int cap = size <= 0 ? 6 : Math.min(size, 50);
        List<Tour> capped = matches.stream().limit(cap).toList();
        List<TourListResponse> tours = tourService.toListResponses(capped);
        Map<Long, GuideChip> guides = new LinkedHashMap<>();
        for (Tour tour : capped) {
            Guide guide = tour.getGuide();
            if (guide == null || guide.getVerificationStatus() != Guide.VerificationStatus.VERIFIED || guide.getUser() == null) {
                continue;
            }
            guides.putIfAbsent(guide.getId(), GuideChip.builder()
                    .id(guide.getId())
                    .firstName(guide.getUser().getFirstName())
                    .profilePictureUrl(guide.getUser().getProfilePictureUrl())
                    .averageRating(guide.getAverageRating())
                    .verified(true)
                    .build());
        }
        return ThingTours.builder().tours(tours).guides(new ArrayList<>(guides.values())).build();
    }

    public int count(List<Tour> matches) {
        return matches.size();
    }

    public BigDecimal fromPriceUsd(List<Tour> matches) {
        return matches.stream()
                .map(Tour::getPricePerPerson)
                .filter(price -> price != null)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    @Transactional
    public void replaceTourLinks(Long tourId, List<TourLinkRequest> links, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthorizedException("Authentication required");
        }
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authentication required"));
        boolean admin = user.getRole() == User.UserRole.ADMIN;
        boolean owner = tour.getGuide() != null
                && tour.getGuide().getUser() != null
                && user.getId().equals(tour.getGuide().getUser().getId());
        if (!admin && !owner) {
            throw new AccessDeniedException("You cannot edit links for this tour");
        }
        List<TourLinkRequest> body = links == null ? List.of() : links;
        for (TourLinkRequest link : body) {
            if (link.getType() == null || link.getId() == null) {
                throw new IllegalArgumentException("Each link needs a type and an id");
            }
            boolean exists = switch (link.getType()) {
                case ACTIVITY -> activityRepository.existsById(link.getId());
                case EVENT -> eventRepository.existsById(link.getId());
                case RESTAURANT -> restaurantRepository.existsById(link.getId());
            };
            if (!exists) {
                throw new IllegalArgumentException("Unknown " + link.getType() + " id " + link.getId());
            }
        }
        linkRepository.deleteByIdTourId(tourId);
        linkRepository.flush();
        for (TourLinkRequest link : body) {
            linkRepository.save(new TourThingLink(tourId, link.getType(), link.getId()));
        }
    }

    private List<Tour> match(ThingType type, Long thingId, List<Tour> activeTours, java.util.function.Predicate<Tour> auto) {
        Map<Long, Tour> byId = new HashMap<>();
        for (Tour tour : activeTours) {
            byId.putIfAbsent(tour.getId(), tour);
        }
        Map<Long, Long> bookings = bookingCounts(byId.keySet());
        Comparator<Tour> order = Comparator
                .comparingLong((Tour tour) -> bookings.getOrDefault(tour.getId(), 0L)).reversed()
                .thenComparing((Tour tour) -> rating(tour), Comparator.reverseOrder())
                .thenComparing(tour -> tour.getTitle() == null ? "" : tour.getTitle(), String.CASE_INSENSITIVE_ORDER);

        LinkedHashMap<Long, Tour> ordered = new LinkedHashMap<>();
        List<Tour> explicit = new ArrayList<>();
        for (TourThingLink link : linkRepository.findByIdThingTypeAndIdThingId(type, thingId)) {
            Tour tour = byId.get(link.getId().getThingId());
            if (tour != null && !isTestTitle(tour.getTitle())) {
                explicit.add(tour);
            }
        }
        explicit.stream().sorted(order).forEach(tour -> ordered.put(tour.getId(), tour));
        activeTours.stream()
                .filter(tour -> !ordered.containsKey(tour.getId()))
                .filter(tour -> !isTestTitle(tour.getTitle()))
                .filter(auto)
                .sorted(order)
                .forEach(tour -> ordered.put(tour.getId(), tour));
        return new ArrayList<>(ordered.values());
    }

    private Map<Long, Long> bookingCounts(Collection<Long> tourIds) {
        if (tourIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : bookingRepository.countBookingsByTourIds(tourIds)) {
            counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }
        return counts;
    }

    private BigDecimal rating(Tour tour) {
        Double average = reviewRepository.getAverageRatingByTourId(tour.getId());
        return average == null ? BigDecimal.ZERO : BigDecimal.valueOf(average);
    }

    private static boolean isTestTitle(String title) {
        return title != null && TEST_TITLE.matcher(title.trim()).matches();
    }

    private static Set<String> names(Destination primary, Collection<Destination> nearby) {
        Set<String> names = new java.util.HashSet<>();
        if (primary != null && primary.getName() != null) {
            names.add(primary.getName().toLowerCase(Locale.ROOT));
        }
        if (nearby != null) {
            for (Destination destination : nearby) {
                if (destination.getName() != null) {
                    names.add(destination.getName().toLowerCase(Locale.ROOT));
                }
            }
        }
        return names;
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
