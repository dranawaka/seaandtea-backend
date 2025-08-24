package com.seaandtea.controller;

import com.seaandtea.dto.GuideProfileRequest;
import com.seaandtea.dto.GuideProfileResponse;
import com.seaandtea.service.GuideService;
import com.seaandtea.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guides")
@RequiredArgsConstructor
@Slf4j
public class GuideController {
    
    private final GuideService guideService;
    private final JwtService jwtService;
    
    /**
     * Create a new guide profile for the authenticated user
     */
    @PostMapping
    public ResponseEntity<GuideProfileResponse> createGuideProfile(
            @Valid @RequestBody GuideProfileRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Creating guide profile for authenticated user");
        
        // Get user ID from JWT token
        Long userId = getCurrentUserId(httpRequest);
        
        GuideProfileResponse response = guideService.createGuideProfile(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Get guide profile by ID
     */
    @GetMapping("/{guideId}")
    public ResponseEntity<GuideProfileResponse> getGuideProfile(@PathVariable Long guideId) {
        log.info("Fetching guide profile with ID: {}", guideId);
        
        GuideProfileResponse response = guideService.getGuideProfile(guideId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get guide profile for the authenticated user
     */
    @GetMapping("/my-profile")
    public ResponseEntity<GuideProfileResponse> getMyGuideProfile(HttpServletRequest httpRequest) {
        log.info("Fetching guide profile for authenticated user");
        
        Long userId = getCurrentUserId(httpRequest);
        GuideProfileResponse response = guideService.getGuideProfileByUserId(userId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update guide profile for the authenticated user
     */
    @PutMapping("/my-profile")
    public ResponseEntity<GuideProfileResponse> updateMyGuideProfile(
            @Valid @RequestBody GuideProfileRequest request,
            HttpServletRequest httpRequest) {
        
        log.info("Updating guide profile for authenticated user");
        
        Long userId = getCurrentUserId(httpRequest);
        GuideProfileResponse currentProfile = guideService.getGuideProfileByUserId(userId);
        
        GuideProfileResponse response = guideService.updateGuideProfile(currentProfile.getId(), request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update guide profile by ID (admin only)
     */
    @PutMapping("/{guideId}")
    public ResponseEntity<GuideProfileResponse> updateGuideProfile(
            @PathVariable Long guideId,
            @Valid @RequestBody GuideProfileRequest request) {
        
        log.info("Updating guide profile with ID: {}", guideId);
        
        GuideProfileResponse response = guideService.updateGuideProfile(guideId, request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete guide profile for the authenticated user
     */
    @DeleteMapping("/my-profile")
    public ResponseEntity<Void> deleteMyGuideProfile(HttpServletRequest httpRequest) {
        log.info("Deleting guide profile for authenticated user");
        
        Long userId = getCurrentUserId(httpRequest);
        GuideProfileResponse currentProfile = guideService.getGuideProfileByUserId(userId);
        
        guideService.deleteGuideProfile(currentProfile.getId());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Delete guide profile by ID (admin only)
     */
    @DeleteMapping("/{guideId}")
    public ResponseEntity<Void> deleteGuideProfile(@PathVariable Long guideId) {
        log.info("Deleting guide profile with ID: {}", guideId);
        
        guideService.deleteGuideProfile(guideId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Check if the authenticated user has a guide profile
     */
    @GetMapping("/my-profile/exists")
    public ResponseEntity<Boolean> hasGuideProfile(HttpServletRequest httpRequest) {
        log.info("Checking if authenticated user has guide profile");
        
        Long userId = getCurrentUserId(httpRequest);
        boolean hasProfile = guideService.hasGuideProfile(userId);
        return ResponseEntity.ok(hasProfile);
    }
    
    /**
     * Get current user ID from JWT token in request
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalStateException("Authorization header not found or invalid");
        }
        
        String token = authHeader.substring(7);
        Long userId = jwtService.extractUserId(token);
        
        if (userId == null) {
            throw new IllegalStateException("User ID not found in token");
        }
        
        return userId;
    }
}
