package com.seaandtea.service;

import com.seaandtea.dto.AvailabilityDayResponse;
import com.seaandtea.dto.BookingCreateRequest;
import com.seaandtea.dto.BookingResponse;
import com.seaandtea.entity.Booking;
import com.seaandtea.entity.Booking.BookingStatus;
import com.seaandtea.entity.Booking.PaymentStatus;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.TourImage;
import com.seaandtea.entity.User;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.BookingRepository;
import com.seaandtea.repository.TourRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final Set<BookingStatus> HOLDING_SPOTS = EnumSet.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
    private static final int MAX_RANGE_DAYS = 92;

    private final BookingRepository bookingRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AvailabilityDayResponse> getAvailability(Long tourId, LocalDate from, LocalDate to) {
        Tour tour = tourRepository.findById(tourId)
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));

        LocalDate start = from == null ? LocalDate.now() : from;
        LocalDate end = to == null ? start.plusDays(60) : to;
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Availability end date is before the start date");
        }
        if (start.plusDays(MAX_RANGE_DAYS).isBefore(end)) {
            end = start.plusDays(MAX_RANGE_DAYS);
        }

        Map<LocalDate, Integer> booked = new HashMap<>();
        for (Object[] row : bookingRepository.sumPeopleBetween(tourId, start, end, HOLDING_SPOTS)) {
            LocalDate date = (LocalDate) row[0];
            int people = row[1] == null ? 0 : ((Number) row[1]).intValue();
            booked.put(date, people);
        }

        int capacity = capacity(tour);
        LocalTime departure = departure(tour);
        List<AvailabilityDayResponse> days = new ArrayList<>();
        for (LocalDate day = start; !day.isAfter(end); day = day.plusDays(1)) {
            int remaining = Math.max(0, capacity - booked.getOrDefault(day, 0));
            days.add(AvailabilityDayResponse.builder()
                    .date(day)
                    .remainingSpots(remaining)
                    .departureTime(departure)
                    .build());
        }
        return days;
    }

    @Transactional
    public BookingResponse createBooking(BookingCreateRequest request, String userEmail) {
        User tourist = requireUser(userEmail);
        Tour tour = tourRepository.findByIdForUpdate(request.getTourId())
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Tour", request.getTourId()));

        if (request.getDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Choose today or a future date");
        }

        int people = request.getNumberOfPeople();
        int capacity = capacity(tour);
        if (people > capacity) {
            throw new IllegalArgumentException("This tour allows at most " + capacity + " people");
        }

        int booked = bookedPeople(tour.getId(), request.getDate());
        int remaining = capacity - booked;
        if (people > remaining) {
            throw new IllegalStateException(remaining <= 0
                    ? "That date is fully booked"
                    : "Only " + remaining + " spots left on that date");
        }

        LocalTime start = departure(tour);
        int hours = tour.getDurationHours() == null ? 1 : Math.max(1, tour.getDurationHours());
        LocalTime end = start.plusHours(hours);
        BigDecimal total = tour.getPricePerPerson()
                .multiply(BigDecimal.valueOf(people))
                .setScale(2, RoundingMode.HALF_UP);

        String note = request.getSpecialRequests() == null ? null : request.getSpecialRequests().trim();
        if (note != null && note.isEmpty()) {
            note = null;
        }

        Booking booking = Booking.builder()
                .tour(tour)
                .tourist(tourist)
                .guide(tour.getGuide())
                .bookingDate(request.getDate())
                .startTime(start)
                .endTime(end)
                .numberOfPeople(people)
                .totalAmount(total)
                .status(BookingStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .specialRequests(note)
                .build();

        booking = bookingRepository.save(booking);
        log.info("Created booking {} for tour {}", booking.getId(), tour.getId());
        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMyBookings(String userEmail) {
        User tourist = requireUser(userEmail);
        return bookingRepository.findByTouristIdOrderByCreatedAtDesc(tourist.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getMyBooking(Long bookingId, String userEmail) {
        User tourist = requireUser(userEmail);
        Booking booking = bookingRepository.findByIdAndTouristId(bookingId, tourist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        return toResponse(booking);
    }

    @Transactional
    public BookingResponse cancelMyBooking(Long bookingId, String userEmail) {
        User tourist = requireUser(userEmail);
        Booking booking = bookingRepository.findByIdAndTouristId(bookingId, tourist.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return toResponse(booking);
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Completed bookings cannot be cancelled");
        }

        if (booking.getPaymentStatus() == PaymentStatus.PAID) {
            LocalDateTime departure = LocalDateTime.of(booking.getBookingDate(), booking.getStartTime());
            if (departure.isBefore(LocalDateTime.now().plusHours(48))) {
                throw new IllegalStateException("Paid bookings can only be cancelled more than 48 hours before departure");
            }
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking = bookingRepository.save(booking);
        log.info("Cancelled booking {}", booking.getId());
        return toResponse(booking);
    }

    @Transactional
    public Booking markPaid(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));
        if (booking.getStatus() != BookingStatus.CANCELLED) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking = bookingRepository.save(booking);
        }
        return booking;
    }

    private int bookedPeople(Long tourId, LocalDate date) {
        Long sum = bookingRepository.sumPeopleOnDate(tourId, date, HOLDING_SPOTS);
        return sum == null ? 0 : sum.intValue();
    }

    private static int capacity(Tour tour) {
        return tour.getMaxGroupSize() == null || tour.getMaxGroupSize() < 1 ? 10 : tour.getMaxGroupSize();
    }

    private static LocalTime departure(Tour tour) {
        return tour.getDepartureTime() == null ? LocalTime.of(9, 0) : tour.getDepartureTime();
    }

    private User requireUser(String email) {
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Sign in required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BookingResponse toResponse(Booking booking) {
        Tour tour = booking.getTour();
        return BookingResponse.builder()
                .id(booking.getId())
                .tourId(tour.getId())
                .tourTitle(tour.getTitle())
                .tourImage(primaryImage(tour))
                .destination(tour.getDestination())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .numberOfPeople(booking.getNumberOfPeople())
                .totalAmount(booking.getTotalAmount())
                .currency("USD")
                .status(booking.getStatus() == null ? null : booking.getStatus().name())
                .paymentStatus(booking.getPaymentStatus() == null ? null : booking.getPaymentStatus().name())
                .specialRequests(booking.getSpecialRequests())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    private static String primaryImage(Tour tour) {
        if (tour.getImages() == null || tour.getImages().isEmpty()) {
            return null;
        }
        return tour.getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .map(TourImage::getImageUrl)
                .findFirst()
                .orElse(tour.getImages().get(0).getImageUrl());
    }
}
