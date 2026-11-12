package com.seaandtea.service;

import com.seaandtea.dto.TourListResponse;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.User;
import com.seaandtea.entity.Wishlist;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.TourRepository;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final TourRepository tourRepository;
    private final UserRepository userRepository;
    private final TourService tourService;

    @Transactional(readOnly = true)
    public List<TourListResponse> list(String userEmail) {
        User user = requireUser(userEmail);
        List<Tour> tours = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(Wishlist::getTour)
                .filter(tour -> tour != null && Boolean.TRUE.equals(tour.getIsActive()))
                .toList();
        return tourService.toListResponses(tours);
    }

    @Transactional
    public void add(Long tourId, String userEmail) {
        User user = requireUser(userEmail);
        Tour tour = tourRepository.findById(tourId)
                .filter(item -> Boolean.TRUE.equals(item.getIsActive()))
                .orElseThrow(() -> new ResourceNotFoundException("Tour", tourId));
        if (wishlistRepository.existsByUserIdAndTourId(user.getId(), tour.getId())) {
            return;
        }
        try {
            wishlistRepository.save(Wishlist.builder().user(user).tour(tour).build());
        } catch (DataIntegrityViolationException ignored) {
            // Another request saved the same pair.
        }
    }

    @Transactional
    public void remove(Long tourId, String userEmail) {
        User user = requireUser(userEmail);
        wishlistRepository.deleteByUserIdAndTourId(user.getId(), tourId);
    }

    private User requireUser(String email) {
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Sign in required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
