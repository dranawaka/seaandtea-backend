package com.seaandtea.service;

import com.seaandtea.dto.GuideProfileRequest;
import com.seaandtea.dto.GuideProfileResponse;
import com.seaandtea.dto.UserRoleUpdateRequest;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.GuideLanguage;
import com.seaandtea.entity.GuideSpecialty;
import com.seaandtea.entity.User;
import com.seaandtea.repository.GuideRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GuideService {
    
    private final GuideRepository guideRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    
    /**
     * Create a new guide profile for a user
     * This method assumes the user already has GUIDE role
     */
    public GuideProfileResponse createGuideProfile(Long userId, GuideProfileRequest request) {
        log.info("Creating guide profile for user ID: {} (user already has GUIDE role)", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        // Check if user already has a guide profile
        if (guideRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has a guide profile");
        }
        
        // Create guide entity
        Guide guide = Guide.builder()
                .user(user)
                .bio(request.getBio())
                .hourlyRate(request.getHourlyRate())
                .dailyRate(request.getDailyRate())
                .responseTimeHours(request.getResponseTimeHours())
                .isAvailable(request.getIsAvailable())
                .verificationStatus(Guide.VerificationStatus.PENDING)
                .verificationDocuments(request.getVerificationDocuments())
                .build();
        
        // Save guide first to get the ID
        Guide savedGuide = guideRepository.save(guide);
        
        // Create and save specialties
        List<GuideSpecialty> specialties = request.getSpecialties().stream()
                .map(specialtyRequest -> GuideSpecialty.builder()
                        .guide(savedGuide)
                        .specialty(specialtyRequest.getSpecialty())
                        .yearsExperience(specialtyRequest.getYearsExperience())
                        .certificationUrl(specialtyRequest.getCertificationUrl())
                        .build())
                .collect(Collectors.toList());
        
        savedGuide.setSpecialties(specialties);
        
        // Create and save languages
        List<GuideLanguage> languages = request.getLanguages().stream()
                .map(languageRequest -> GuideLanguage.builder()
                        .guide(savedGuide)
                        .language(languageRequest.getLanguage())
                        .proficiencyLevel(GuideLanguage.ProficiencyLevel.valueOf(
                                languageRequest.getProficiencyLevel().toUpperCase()))
                        .build())
                .collect(Collectors.toList());
        
        savedGuide.setLanguages(languages);
        
        // Save the complete guide with relationships
        final Guide finalGuide = guideRepository.save(savedGuide);
        
        log.info("Successfully created guide profile with ID: {}", finalGuide.getId());
        return mapToResponse(finalGuide);
    }
    
    /**
     * Upgrade user to GUIDE role and create their first guide profile
     * This method is for users who want to become guides
     */
    public GuideProfileResponse upgradeToGuideAndCreateProfile(Long userId, GuideProfileRequest request) {
        log.info("Upgrading user ID: {} to GUIDE role and creating profile", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        
        // Check if user already has a guide profile
        if (guideRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("User already has a guide profile");
        }
        
        // Update user role to GUIDE
        userService.updateUserRole(userId, UserRoleUpdateRequest.builder()
                .role("GUIDE")
                .build());
        
        // Create guide entity
        Guide guide = Guide.builder()
                .user(user)
                .bio(request.getBio())
                .hourlyRate(request.getHourlyRate())
                .dailyRate(request.getDailyRate())
                .responseTimeHours(request.getResponseTimeHours())
                .isAvailable(request.getIsAvailable())
                .verificationStatus(Guide.VerificationStatus.PENDING)
                .verificationDocuments(request.getVerificationDocuments())
                .build();
        
        // Save guide first to get the ID
        Guide savedGuide = guideRepository.save(guide);
        
        // Create and save specialties
        List<GuideSpecialty> specialties = request.getSpecialties().stream()
                .map(specialtyRequest -> GuideSpecialty.builder()
                        .guide(savedGuide)
                        .specialty(specialtyRequest.getSpecialty())
                        .yearsExperience(specialtyRequest.getYearsExperience())
                        .certificationUrl(specialtyRequest.getCertificationUrl())
                        .build())
                .collect(Collectors.toList());
        
        savedGuide.setSpecialties(specialties);
        
        // Create and save languages
        List<GuideLanguage> languages = request.getLanguages().stream()
                .map(languageRequest -> GuideLanguage.builder()
                        .guide(savedGuide)
                        .language(languageRequest.getLanguage())
                        .proficiencyLevel(GuideLanguage.ProficiencyLevel.valueOf(
                                languageRequest.getProficiencyLevel().toUpperCase()))
                        .build())
                .collect(Collectors.toList());
        
        savedGuide.setLanguages(languages);
        
        // Save the complete guide with relationships
        final Guide finalGuide = guideRepository.save(savedGuide);
        
        log.info("Successfully upgraded user to GUIDE and created guide profile with ID: {}", finalGuide.getId());
        return mapToResponse(finalGuide);
    }
    
    /**
     * Update an existing guide profile
     */
    public GuideProfileResponse updateGuideProfile(Long guideId, GuideProfileRequest request) {
        log.info("Updating guide profile with ID: {}", guideId);
        
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + guideId));
        
        // Update basic guide information
        guide.setBio(request.getBio());
        guide.setHourlyRate(request.getHourlyRate());
        guide.setDailyRate(request.getDailyRate());
        guide.setResponseTimeHours(request.getResponseTimeHours());
        guide.setIsAvailable(request.getIsAvailable());
        guide.setVerificationDocuments(request.getVerificationDocuments());
        
        // Clear existing specialties and languages
        guide.getSpecialties().clear();
        guide.getLanguages().clear();
        
        // Add new specialties
        List<GuideSpecialty> specialties = request.getSpecialties().stream()
                .map(specialtyRequest -> GuideSpecialty.builder()
                        .guide(guide)
                        .specialty(specialtyRequest.getSpecialty())
                        .yearsExperience(specialtyRequest.getYearsExperience())
                        .certificationUrl(specialtyRequest.getCertificationUrl())
                        .build())
                .collect(Collectors.toList());
        
        guide.setSpecialties(specialties);
        
        // Add new languages
        List<GuideLanguage> languages = request.getLanguages().stream()
                .map(languageRequest -> GuideLanguage.builder()
                        .guide(guide)
                        .language(languageRequest.getLanguage())
                        .proficiencyLevel(GuideLanguage.ProficiencyLevel.valueOf(
                                languageRequest.getProficiencyLevel().toUpperCase()))
                        .build())
                .collect(Collectors.toList());
        
        guide.setLanguages(languages);
        
        // Save the updated guide
        final Guide updatedGuide = guideRepository.save(guide);
        
        log.info("Successfully updated guide profile with ID: {}", updatedGuide.getId());
        return mapToResponse(updatedGuide);
    }
    
    /**
     * Get guide profile by ID
     */
    @Transactional(readOnly = true)
    public GuideProfileResponse getGuideProfile(Long guideId) {
        log.info("Fetching guide profile with ID: {}", guideId);
        
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + guideId));
        
        return mapToResponse(guide);
    }
    
    /**
     * Get guide profile by user ID
     */
    @Transactional(readOnly = true)
    public GuideProfileResponse getGuideProfileByUserId(Long userId) {
        log.info("Fetching guide profile for user ID: {}", userId);
        
        Guide guide = guideRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Guide profile not found for user ID: " + userId));
        
        return mapToResponse(guide);
    }
    
    /**
     * Check if user has a guide profile
     */
    @Transactional(readOnly = true)
    public boolean hasGuideProfile(Long userId) {
        return guideRepository.findByUserId(userId).isPresent();
    }
    
    /**
     * Delete guide profile
     */
    public void deleteGuideProfile(Long guideId) {
        log.info("Deleting guide profile with ID: {}", guideId);
        
        if (!guideRepository.existsById(guideId)) {
            throw new IllegalArgumentException("Guide not found with ID: " + guideId);
        }
        
        guideRepository.deleteById(guideId);
        log.info("Successfully deleted guide profile with ID: {}", guideId);
    }
    
    /**
     * Get guides by verification status
     */
    @Transactional(readOnly = true)
    public List<GuideProfileResponse> getGuidesByVerificationStatus(Guide.VerificationStatus status) {
        log.info("Fetching guides with verification status: {}", status);
        
        List<Guide> guides = guideRepository.findByVerificationStatus(status);
        return guides.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get guides by verification status with pagination
     */
    @Transactional(readOnly = true)
    public Page<GuideProfileResponse> getGuidesByVerificationStatusPaginated(
            Guide.VerificationStatus status, Pageable pageable) {
        log.info("Fetching guides with verification status: {} (page: {}, size: {})", 
                status, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Guide> guides = guideRepository.findByVerificationStatus(status, pageable);
        return guides.map(this::mapToResponse);
    }
    
    /**
     * Get all guides
     */
    @Transactional(readOnly = true)
    public List<GuideProfileResponse> getAllGuides() {
        log.info("Fetching all guides");
        
        List<Guide> guides = guideRepository.findAll();
        return guides.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all verified guides (public endpoint)
     */
    @Transactional(readOnly = true)
    public List<GuideProfileResponse> getAllVerifiedGuides() {
        log.info("Fetching all verified guides for public endpoint");
        
        List<Guide> verifiedGuides = guideRepository.findByVerificationStatus(Guide.VerificationStatus.VERIFIED);
        return verifiedGuides.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get verified guides with pagination (public endpoint)
     */
    @Transactional(readOnly = true)
    public Page<GuideProfileResponse> getVerifiedGuidesPaginated(Pageable pageable) {
        log.info("Fetching verified guides with pagination: page {}, size {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Guide> verifiedGuides = guideRepository.findByVerificationStatus(Guide.VerificationStatus.VERIFIED, pageable);
        return verifiedGuides.map(this::mapToResponse);
    }
    
    /**
     * Verify guide profile (approve)
     */
    public GuideProfileResponse verifyGuideProfile(Long guideId) {
        log.info("Verifying guide profile with ID: {}", guideId);
        
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + guideId));
        
        if (guide.getVerificationStatus() == Guide.VerificationStatus.VERIFIED) {
            throw new IllegalStateException("Guide is already verified");
        }
        
        guide.setVerificationStatus(Guide.VerificationStatus.VERIFIED);
        Guide updatedGuide = guideRepository.save(guide);
        
        log.info("Successfully verified guide profile with ID: {}", guideId);
        return mapToResponse(updatedGuide);
    }
    
    /**
     * Reject guide profile
     */
    public GuideProfileResponse rejectGuideProfile(Long guideId, String reason) {
        log.info("Rejecting guide profile with ID: {} for reason: {}", guideId, reason);
        
        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + guideId));
        
        if (guide.getVerificationStatus() == Guide.VerificationStatus.REJECTED) {
            throw new IllegalStateException("Guide is already rejected");
        }
        
        guide.setVerificationStatus(Guide.VerificationStatus.REJECTED);
        // You could add a rejection reason field to the Guide entity if needed
        Guide updatedGuide = guideRepository.save(guide);
        
        log.info("Successfully rejected guide profile with ID: {}", guideId);
        return mapToResponse(updatedGuide);
    }
    
    /**
     * Map Guide entity to GuideProfileResponse DTO
     */
    private GuideProfileResponse mapToResponse(Guide guide) {
        return GuideProfileResponse.builder()
                .id(guide.getId())
                .userId(guide.getUser().getId())
                .userFirstName(guide.getUser().getFirstName())
                .userLastName(guide.getUser().getLastName())
                .userEmail(guide.getUser().getEmail())
                .profilePictureUrl(guide.getUser().getProfilePictureUrl())
                .bio(guide.getBio())
                .hourlyRate(guide.getHourlyRate())
                .dailyRate(guide.getDailyRate())
                .responseTimeHours(guide.getResponseTimeHours())
                .isAvailable(guide.getIsAvailable())
                .totalTours(guide.getTotalTours())
                .averageRating(guide.getAverageRating())
                .totalReviews(guide.getTotalReviews())
                .verificationStatus(guide.getVerificationStatus().name())
                .verificationDocuments(guide.getVerificationDocuments())
                .createdAt(guide.getCreatedAt())
                .updatedAt(guide.getUpdatedAt())
                .specialties(guide.getSpecialties().stream()
                        .map(this::mapSpecialtyToResponse)
                        .collect(Collectors.toList()))
                .languages(guide.getLanguages().stream()
                        .map(this::mapLanguageToResponse)
                        .collect(Collectors.toList()))
                .build();
    }
    
    private GuideProfileResponse.GuideSpecialtyResponse mapSpecialtyToResponse(GuideSpecialty specialty) {
        return GuideProfileResponse.GuideSpecialtyResponse.builder()
                .id(specialty.getId())
                .specialty(specialty.getSpecialty())
                .yearsExperience(specialty.getYearsExperience())
                .certificationUrl(specialty.getCertificationUrl())
                .createdAt(specialty.getCreatedAt())
                .build();
    }
    
    private GuideProfileResponse.GuideLanguageResponse mapLanguageToResponse(GuideLanguage language) {
        return GuideProfileResponse.GuideLanguageResponse.builder()
                .id(language.getId())
                .language(language.getLanguage())
                .proficiencyLevel(language.getProficiencyLevel().name())
                .createdAt(language.getCreatedAt())
                .build();
    }
}
