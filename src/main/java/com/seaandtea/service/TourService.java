package com.seaandtea.service;

import com.seaandtea.dto.*;
import com.seaandtea.entity.*;
import com.seaandtea.entity.Tour.TourCategory;
import com.seaandtea.entity.User.UserRole;
import com.seaandtea.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourService {
    
    private final TourRepository tourRepository;
    private final TourImageRepository tourImageRepository;
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    
    @Transactional
    public TourResponse createTour(TourCreateRequest request, String userEmail) {
        log.info("Creating tour for user: {}", userEmail);
        
        // Find the user and their guide profile
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Guide guide = guideRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Guide profile not found"));
        
        // Create tour entity
        Tour tour = Tour.builder()
            .guide(guide)
            .title(request.getTitle())
            .description(request.getDescription())
            .category(request.getCategory())
            .durationHours(request.getDurationHours())
            .maxGroupSize(request.getMaxGroupSize())
            .pricePerPerson(request.getPricePerPerson())
            .instantBooking(request.getInstantBooking())
            .securePayment(request.getSecurePayment())
            .meetingPoint(request.getMeetingPoint())
            .cancellationPolicy(request.getCancellationPolicy())
            .isActive(true)
            .languages(request.getLanguages())
            .highlights(request.getHighlights())
            .includedItems(request.getIncludedItems())
            .excludedItems(request.getExcludedItems())
            .build();
        
        // Save tour
        tour = tourRepository.save(tour);
        
        // Handle images
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            addImagesToTour(tour, request.getImageUrls(), request.getPrimaryImageIndex());
        }
        
        // Update guide total tours count
        guide.setTotalTours(guide.getTotalTours() + 1);
        guideRepository.save(guide);
        
        log.info("Tour created successfully with ID: {}", tour.getId());
        return convertToTourResponse(tour);
    }
    
    @Transactional
    public TourResponse updateTour(Long tourId, TourUpdateRequest request, String userEmail) {
        log.info("Updating tour {} for user: {}", tourId, userEmail);
        
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new RuntimeException("Tour not found"));
        
        // Check if user owns the tour
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!tour.getGuide().getUser().getId().equals(user.getId()) && 
            !user.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to update this tour");
        }
        
        // Update fields if provided
        if (request.getTitle() != null) {
            tour.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            tour.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            tour.setCategory(request.getCategory());
        }
        if (request.getDurationHours() != null) {
            tour.setDurationHours(request.getDurationHours());
        }
        if (request.getMaxGroupSize() != null) {
            tour.setMaxGroupSize(request.getMaxGroupSize());
        }
        if (request.getPricePerPerson() != null) {
            tour.setPricePerPerson(request.getPricePerPerson());
        }
        if (request.getInstantBooking() != null) {
            tour.setInstantBooking(request.getInstantBooking());
        }
        if (request.getSecurePayment() != null) {
            tour.setSecurePayment(request.getSecurePayment());
        }
        if (request.getMeetingPoint() != null) {
            tour.setMeetingPoint(request.getMeetingPoint());
        }
        if (request.getCancellationPolicy() != null) {
            tour.setCancellationPolicy(request.getCancellationPolicy());
        }
        if (request.getIsActive() != null) {
            tour.setIsActive(request.getIsActive());
        }
        
        // Update JSON fields if provided
        if (request.getLanguages() != null) {
            tour.setLanguages(request.getLanguages().isEmpty() ? null : request.getLanguages());
        }
        if (request.getHighlights() != null) {
            tour.setHighlights(request.getHighlights().isEmpty() ? null : request.getHighlights());
        }
        if (request.getIncludedItems() != null) {
            tour.setIncludedItems(request.getIncludedItems().isEmpty() ? null : request.getIncludedItems());
        }
        if (request.getExcludedItems() != null) {
            tour.setExcludedItems(request.getExcludedItems().isEmpty() ? null : request.getExcludedItems());
        }
        
        // Handle image updates
        if (request.getImageUrls() != null) {
            // Remove existing images
            tourImageRepository.deleteByTourId(tourId);
            // Add new images
            if (!request.getImageUrls().isEmpty()) {
                addImagesToTour(tour, request.getImageUrls(), request.getPrimaryImageIndex());
            }
        }
        
        tour = tourRepository.save(tour);
        log.info("Tour {} updated successfully", tourId);
        return convertToTourResponse(tour);
    }
    
    @Transactional(readOnly = true)
    public TourResponse getTourById(Long tourId) {
        Tour tour = tourRepository.findActiveByIdWithImages(tourId)
            .orElseThrow(() -> new RuntimeException("Tour not found"));
        return convertToTourResponse(tour);
    }
    
    @Transactional(readOnly = true)
    public Page<TourListResponse> getToursWithFilters(TourFilterRequest filterRequest) {
        Sort sort = Sort.by(
            filterRequest.getSortDirection().equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC,
            filterRequest.getSortBy()
        );
        
        Pageable pageable = PageRequest.of(
            filterRequest.getPage(), 
            filterRequest.getSize(), 
            sort
        );
        
        Page<Tour> tours;
        
        if (hasFilters(filterRequest)) {
            tours = tourRepository.findActiveToursWithFilters(
                filterRequest.getCategory(),
                filterRequest.getMinPrice(),
                filterRequest.getMaxPrice(),
                filterRequest.getMinDuration(),
                filterRequest.getMaxDuration(),
                filterRequest.getSearchTerm(),
                pageable
            );
        } else {
            tours = tourRepository.findActiveToursWithImages(pageable);
        }
        
        return tours.map(this::convertToTourListResponse);
    }
    
    @Transactional(readOnly = true)
    public List<TourResponse> getToursByGuide(Long guideId) {
        List<Tour> tours = tourRepository.findActiveByGuideIdWithImages(guideId);
        return tours.stream()
            .map(this::convertToTourResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<TourResponse> getMyTours(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Guide guide = guideRepository.findByUserId(user.getId())
            .orElseThrow(() -> new RuntimeException("Guide profile not found"));
        
        List<Tour> tours = tourRepository.findAllByGuideIdOrderByCreatedAtDesc(guide.getId());
        return tours.stream()
            .map(this::convertToTourResponse)
            .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteTour(Long tourId, String userEmail) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new RuntimeException("Tour not found"));
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!tour.getGuide().getUser().getId().equals(user.getId()) && 
            !user.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to delete this tour");
        }
        
        // Soft delete - set as inactive
        tour.setIsActive(false);
        tourRepository.save(tour);
        
        log.info("Tour {} marked as inactive", tourId);
    }
    
    @Transactional
    public TourResponse addImageToTour(Long tourId, TourImageUploadRequest request, String userEmail) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new RuntimeException("Tour not found"));
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!tour.getGuide().getUser().getId().equals(user.getId()) && 
            !user.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to modify this tour");
        }
        
        // Check if image already exists
        if (tourImageRepository.existsByTourIdAndImageUrl(tourId, request.getImageUrl())) {
            throw new RuntimeException("Image already exists for this tour");
        }
        
        // If setting as primary, make all others non-primary
        if (request.getIsPrimary()) {
            tourImageRepository.setAllNonPrimaryForTour(tourId);
        }
        
        TourImage image = TourImage.builder()
            .tour(tour)
            .imageUrl(request.getImageUrl())
            .isPrimary(request.getIsPrimary())
            .altText(request.getAltText())
            .build();
        
        tourImageRepository.save(image);
        
        return convertToTourResponse(tour);
    }
    
    @Transactional
    public void removeImageFromTour(Long tourId, Long imageId, String userEmail) {
        Tour tour = tourRepository.findById(tourId)
            .orElseThrow(() -> new RuntimeException("Tour not found"));
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!tour.getGuide().getUser().getId().equals(user.getId()) && 
            !user.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("You don't have permission to modify this tour");
        }
        
        TourImage image = tourImageRepository.findById(imageId)
            .orElseThrow(() -> new RuntimeException("Image not found"));
        
        if (!image.getTour().getId().equals(tourId)) {
            throw new RuntimeException("Image does not belong to this tour");
        }
        
        tourImageRepository.delete(image);
    }
    
    @Transactional(readOnly = true)
    public Page<TourListResponse> getPopularTours(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tour> tours = tourRepository.findMostPopularActiveTours(pageable);
        return tours.map(this::convertToTourListResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<TourListResponse> getToursByCategory(TourCategory category, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Tour> tours = tourRepository.findActiveByCategoryOrderByCreatedAtDesc(category, pageable);
        return tours.map(this::convertToTourListResponse);
    }
    
    @Transactional(readOnly = true)
    public Map<String, Object> getTourStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Count by category
        Map<TourCategory, Long> categoryStats = new HashMap<>();
        for (TourCategory category : TourCategory.values()) {
            categoryStats.put(category, tourRepository.countActiveByCategory(category));
        }
        stats.put("categoryStats", categoryStats);
        
        // Total tours
        stats.put("totalActiveTours", tourRepository.count());
        
        return stats;
    }
    
    // Private helper methods
    
    private void addImagesToTour(Tour tour, List<String> imageUrls, Integer primaryIndex) {
        for (int i = 0; i < imageUrls.size(); i++) {
            TourImage image = TourImage.builder()
                .tour(tour)
                .imageUrl(imageUrls.get(i))
                .isPrimary(primaryIndex != null && primaryIndex == i)
                .build();
            tourImageRepository.save(image);
        }
    }
    
    private boolean hasFilters(TourFilterRequest filterRequest) {
        return filterRequest.getCategory() != null ||
               filterRequest.getMinPrice() != null ||
               filterRequest.getMaxPrice() != null ||
               filterRequest.getMinDuration() != null ||
               filterRequest.getMaxDuration() != null ||
               (filterRequest.getSearchTerm() != null && !filterRequest.getSearchTerm().trim().isEmpty()) ||
               filterRequest.getInstantBooking() != null;
    }
    
    private TourResponse convertToTourResponse(Tour tour) {
        TourResponse.GuideBasicInfo guideInfo = TourResponse.GuideBasicInfo.builder()
            .id(tour.getGuide().getId())
            .firstName(tour.getGuide().getUser().getFirstName())
            .lastName(tour.getGuide().getUser().getLastName())
            .profilePictureUrl(tour.getGuide().getUser().getProfilePictureUrl())
            .averageRating(tour.getGuide().getAverageRating())
            .totalTours(tour.getGuide().getTotalTours())
            .isVerified(tour.getGuide().getUser().getIsVerified())
            .build();
        
        List<TourResponse.TourImageDto> imageDtos = new ArrayList<>();
        if (tour.getImages() != null) {
            imageDtos = tour.getImages().stream()
                .map(image -> TourResponse.TourImageDto.builder()
                    .id(image.getId())
                    .imageUrl(image.getImageUrl())
                    .isPrimary(image.getIsPrimary())
                    .altText(image.getAltText())
                    .createdAt(image.getCreatedAt())
                    .build())
                .collect(Collectors.toList());
        }
        
        return TourResponse.builder()
            .id(tour.getId())
            .title(tour.getTitle())
            .description(tour.getDescription())
            .category(tour.getCategory())
            .durationHours(tour.getDurationHours())
            .maxGroupSize(tour.getMaxGroupSize())
            .pricePerPerson(tour.getPricePerPerson())
            .instantBooking(tour.getInstantBooking())
            .securePayment(tour.getSecurePayment())
            .languages(tour.getLanguages())
            .highlights(tour.getHighlights())
            .includedItems(tour.getIncludedItems())
            .excludedItems(tour.getExcludedItems())
            .meetingPoint(tour.getMeetingPoint())
            .cancellationPolicy(tour.getCancellationPolicy())
            .isActive(tour.getIsActive())
            .createdAt(tour.getCreatedAt())
            .updatedAt(tour.getUpdatedAt())
            .guide(guideInfo)
            .images(imageDtos)
            .totalBookings(0L)
            .averageRating(getTourAverageRating(tour.getId()))
            .totalReviews(reviewRepository.countByTourId(tour.getId()))
            .build();
    }
    
    private BigDecimal getTourAverageRating(Long tourId) {
        Double avg = reviewRepository.getAverageRatingByTourId(tourId);
        return avg != null ? BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    private TourListResponse convertToTourListResponse(Tour tour) {
        String primaryImageUrl = null;
        if (tour.getImages() != null && !tour.getImages().isEmpty()) {
            primaryImageUrl = tour.getImages().stream()
                .filter(TourImage::getIsPrimary)
                .findFirst()
                .map(TourImage::getImageUrl)
                .orElse(tour.getImages().get(0).getImageUrl());
        }
        
        String highlightsPreview = "";
        List<String> highlights = tour.getHighlights();
        if (highlights != null && !highlights.isEmpty()) {
            highlightsPreview = highlights.stream()
                .limit(3)
                .collect(Collectors.joining(", "));
        }
        
        return TourListResponse.builder()
            .id(tour.getId())
            .title(tour.getTitle())
            .description(tour.getDescription().length() > 150 ? 
                tour.getDescription().substring(0, 150) + "..." : tour.getDescription())
            .category(tour.getCategory())
            .durationHours(tour.getDurationHours())
            .maxGroupSize(tour.getMaxGroupSize())
            .pricePerPerson(tour.getPricePerPerson())
            .instantBooking(tour.getInstantBooking())
            .primaryImageUrl(primaryImageUrl)
            .createdAt(tour.getCreatedAt())
            .guideName(tour.getGuide().getUser().getFirstName() + " " + 
                      tour.getGuide().getUser().getLastName())
            .guideProfilePicture(tour.getGuide().getUser().getProfilePictureUrl())
            .guideAverageRating(tour.getGuide().getAverageRating())
            .guideIsVerified(tour.getGuide().getUser().getIsVerified())
            .averageRating(getTourAverageRating(tour.getId()))
            .totalReviews(reviewRepository.countByTourId(tour.getId()))
            .totalBookings(0L)
            .highlightsPreview(highlightsPreview)
            .build();
    }
    
    /**
     * Get all active tours from verified guides (public endpoint)
     */
    public Page<TourListResponse> getVerifiedTours(Pageable pageable) {
        log.info("Fetching verified tours with pagination: page {}, size {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Tour> verifiedTours = tourRepository.findActiveToursFromVerifiedGuidesWithImages(pageable);
        return verifiedTours.map(this::convertToTourListResponse);
    }
    
    /**
     * Get all active tours from verified guides without pagination (public endpoint)
     */
    public List<TourResponse> getAllVerifiedTours() {
        log.info("Fetching all verified tours");
        
        // Use a large page size to get all tours
        Pageable pageable = PageRequest.of(0, 1000, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Tour> verifiedTours = tourRepository.findActiveToursFromVerifiedGuidesWithImages(pageable);
        return verifiedTours.getContent().stream()
                .map(this::convertToTourResponse)
                .collect(Collectors.toList());
    }
    

}
