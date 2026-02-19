package com.seaandtea.service;

import com.seaandtea.dto.AdminUserResponse;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.User;
import com.seaandtea.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final GuideRepository guideRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final MessageRepository messageRepository;
    private final PaymentRepository paymentRepository;
    private final TourRepository tourRepository;
    private final GuideSpecialtyRepository guideSpecialtyRepository;
    private final GuideLanguageRepository guideLanguageRepository;

    public Page<AdminUserResponse> listUsers(User.UserRole roleFilter, Boolean isActiveFilter, Pageable pageable) {
        Page<User> users = userRepository.findAllForAdmin(roleFilter, isActiveFilter, pageable);
        return users.map(this::mapToAdminResponse);
    }

    public AdminUserResponse getUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        return mapToAdminResponse(user);
    }

    public AdminUserResponse banUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (User.UserRole.ADMIN.equals(user.getRole())) {
            throw new RuntimeException("Cannot ban an admin user");
        }
        user.setIsActive(false);
        User saved = userRepository.save(user);
        return mapToAdminResponse(saved);
    }

    public AdminUserResponse unbanUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setIsActive(true);
        User saved = userRepository.save(user);
        return mapToAdminResponse(saved);
    }

    @Transactional
    public void removeUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (User.UserRole.ADMIN.equals(user.getRole())) {
            throw new RuntimeException("Cannot remove an admin user");
        }

        Long userId = user.getId();
        Guide guide = guideRepository.findByUserId(userId).orElse(null);

        // 1. Messages where user is sender or receiver
        messageRepository.deleteBySenderIdOrReceiverId(userId);

        // 2. Reviews by tourist or by guide
        reviewRepository.deleteByTouristId(userId);
        if (guide != null) {
            reviewRepository.deleteByGuideId(guide.getId());
        }

        // 3. Bookings: collect unique ids, delete messages/payments then bookings
        Set<Long> bookingIds = new LinkedHashSet<>();
        bookingRepository.findByTouristId(userId).stream().map(com.seaandtea.entity.Booking::getId).forEach(bookingIds::add);
        if (guide != null) {
            bookingRepository.findByGuideId(guide.getId()).stream().map(com.seaandtea.entity.Booking::getId).forEach(bookingIds::add);
        }
        for (Long bid : bookingIds) {
            messageRepository.deleteByBookingId(bid);
            paymentRepository.deleteByBookingId(bid);
        }
        bookingRepository.deleteAllById(bookingIds);

        // 4. If guide: tours (cascade deletes tour images), then specialties/languages, then guide
        if (guide != null) {
            Long guideId = guide.getId();
            List<com.seaandtea.entity.Tour> tours = tourRepository.findAllByGuideIdOrderByCreatedAtDesc(guideId);
            tourRepository.deleteAll(tours);
            guideSpecialtyRepository.deleteByGuideId(guideId);
            guideLanguageRepository.deleteByGuideId(guideId);
            guideRepository.delete(guide);
        }

        // 5. User
        userRepository.delete(user);
    }

    private AdminUserResponse mapToAdminResponse(User user) {
        AdminUserResponse.GuideSummary guideSummary = null;
        var guideOpt = guideRepository.findByUserId(user.getId());
        if (guideOpt.isPresent()) {
            Guide g = guideOpt.get();
            guideSummary = AdminUserResponse.GuideSummary.builder()
                    .guideId(g.getId())
                    .verificationStatus(g.getVerificationStatus() != null ? g.getVerificationStatus().name() : null)
                    .isAvailable(g.getIsAvailable())
                    .totalTours(g.getTotalTours())
                    .averageRating(g.getAverageRating() != null ? g.getAverageRating() : BigDecimal.ZERO)
                    .totalReviews(g.getTotalReviews() != null ? g.getTotalReviews() : 0)
                    .build();
        }
        return AdminUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .nationality(user.getNationality())
                .profilePictureUrl(user.getProfilePictureUrl())
                .isVerified(user.getIsVerified())
                .isActive(user.getIsActive())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .guideSummary(guideSummary)
                .build();
    }
}
