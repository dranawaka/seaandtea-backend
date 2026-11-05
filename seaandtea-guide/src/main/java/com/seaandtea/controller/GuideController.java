package com.seaandtea.controller;

import com.seaandtea.dto.GuideProfileRequest;
import com.seaandtea.dto.GuideProfileResponse;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.User;
import com.seaandtea.service.GuideService;
import com.seaandtea.service.FileUploadService;
import com.seaandtea.service.UserService;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/guides")
@Slf4j
public class GuideController {
    
    private final GuideService guideService;
    private final FileUploadService fileUploadService;
    private final UserService userService;
    
    // Constructor logging
    public GuideController(GuideService guideService, FileUploadService fileUploadService, UserService userService) {
        this.guideService = guideService;
        this.fileUploadService = fileUploadService;
        this.userService = userService;
        log.info("=== GUIDE CONTROLLER CONSTRUCTOR CALLED ===");
        log.info("GuideService: {}", guideService != null ? "INJECTED" : "NULL");
        log.info("FileUploadService: {}", fileUploadService != null ? "INJECTED" : "NULL");
        log.info("UserService: {}", userService != null ? "INJECTED" : "NULL");
    }
    
    // Log when controller is initialized
    {
        log.info("=== GUIDE CONTROLLER INITIALIZED ===");
        log.info("Base mapping: /api/v1/guides");
        log.info("Full URL: /api/v1/guides");
        log.info("Available endpoints:");
        log.info("  POST   /api/v1/guides                    - Create guide profile (GUIDE/ADMIN only)");
        log.info("  POST   /api/v1/guides/upgrade            - Upgrade to GUIDE and create profile (USER only)");
        log.info("  GET    /api/v1/guides/ping");
        log.info("  GET    /api/v1/guides/health");
        log.info("  GET    /api/v1/guides/test");
        log.info("  GET    /api/v1/guides/{guideId}");
        log.info("  GET    /api/v1/guides/my-profile");
        log.info("  PUT    /api/v1/guides/my-profile");
        log.info("  PUT    /api/v1/guides/{guideId}");
        log.info("  DELETE /api/v1/guides/my-profile");
        log.info("  DELETE /api/v1/guides/{guideId}");
        log.info("  GET    /api/v1/guides/my-profile/exists - Get guide profile if exists (returns profile or 404)");
        log.info("  POST   /api/v1/guides/my-profile/picture - Upload guide profile picture (GUIDE only)");
        log.info("  GET    /api/v1/guides/test-public        - Test public endpoint (PUBLIC - no auth required)");
        log.info("  GET    /api/v1/guides/verified           - Get all verified guides (PUBLIC - no auth required)");
        log.info("  GET    /api/v1/guides/verified/paginated - Get verified guides with pagination (PUBLIC - no auth required)");
        log.info("  GET    /api/v1/guides?verificationStatus=PENDING - Fetch unverified guides (ADMIN only)");
        log.info("  POST   /api/v1/guides/{id}/verify - Approve guide profile (ADMIN only)");
    }
    
    /**
     * Log request details including headers, body, and parameters
     */
    private void logRequest(HttpServletRequest request, Object body, String... pathVariables) {
        log.info("=== REQUEST RECEIVED ===");
        log.info("Method: {}", request.getMethod());
        log.info("URI: {}", request.getRequestURI());
        log.info("Full URL: {}://{}{}{}", 
                request.getScheme(), 
                request.getServerName(), 
                request.getServerPort() != 80 ? ":" + request.getServerPort() : "",
                request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
        log.info("Query String: {}", request.getQueryString() != null ? request.getQueryString() : "N/A");
        log.info("Remote Address: {}", request.getRemoteAddr());
        log.info("Remote Host: {}", request.getRemoteHost());
        log.info("Remote Port: {}", request.getRemotePort());
        log.info("Local Address: {}", request.getLocalAddr());
        log.info("Local Port: {}", request.getLocalPort());
        log.info("Server Name: {}", request.getServerName());
        log.info("Server Port: {}", request.getServerPort());
        
        // Log all headers
        log.info("=== REQUEST HEADERS ===");
        java.util.Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            if ("authorization".equalsIgnoreCase(headerName)) {
                log.info("{}: {}", headerName, headerValue != null ? "PRESENT (length: " + headerValue.length() + ")" : "NOT PRESENT");
            } else {
                log.info("{}: {}", headerName, headerValue);
            }
        }
        
        log.info("Content Type: {}", request.getContentType());
        log.info("Content Length: {}", request.getContentLength());
        log.info("Character Encoding: {}", request.getCharacterEncoding());
        log.info("Protocol: {}", request.getProtocol());
        log.info("Scheme: {}", request.getScheme());
        
        if (pathVariables.length > 0) {
            log.info("=== PATH VARIABLES ===");
            for (String pathVar : pathVariables) {
                log.info("Path Variable: {}", pathVar);
            }
        }
        
        // Log request parameters
        log.info("=== REQUEST PARAMETERS ===");
        java.util.Enumeration<String> paramNames = request.getParameterNames();
        boolean hasParams = false;
        while (paramNames.hasMoreElements()) {
            hasParams = true;
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                log.info("Parameter {}: {}", paramName, paramValues[0]);
            } else {
                log.info("Parameter {}: [{}]", paramName, String.join(", ", paramValues));
            }
        }
        if (!hasParams) {
            log.info("No request parameters");
        }
        
        if (body != null) {
            log.info("=== REQUEST BODY ===");
            try {
                // Use Jackson ObjectMapper for pretty printing if available
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String prettyBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                log.info("Request Body:\n{}", prettyBody);
            } catch (Exception e) {
                // Fallback to toString if Jackson is not available
                log.info("Request Body: {}", body);
                log.info("Body Class: {}", body.getClass().getName());
            }
        } else {
            log.info("=== REQUEST BODY ===");
            log.info("No request body");
        }
        
        log.info("=== END REQUEST ===");
    }
    
    /**
     * Log response details including status and body
     */
    private void logResponse(HttpStatus status, Object body) {
        log.info("=== RESPONSE SENT ===");
        log.info("Status: {} ({})", status.value(), status.getReasonPhrase());
        log.info("Status Series: {}", status.series());
        log.info("Status Code: {}", status.value());
        
        if (body != null) {
            log.info("=== RESPONSE BODY ===");
            try {
                // Use Jackson ObjectMapper for pretty printing if available
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                String prettyBody = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(body);
                log.info("Response Body:\n{}", prettyBody);
                log.info("Response Body Class: {}", body.getClass().getName());
                
                // Additional response analysis
                if (body instanceof java.util.Collection) {
                    log.info("Response is a Collection with {} items", ((java.util.Collection<?>) body).size());
                } else if (body instanceof java.util.Map) {
                    log.info("Response is a Map with {} entries", ((java.util.Map<?, ?>) body).size());
                }
            } catch (Exception e) {
                // Fallback to toString if Jackson is not available
                log.info("Response Body: {}", body);
                log.info("Response Body Class: {}", body.getClass().getName());
            }
        } else {
            log.info("=== RESPONSE BODY ===");
            log.info("No response body (void response)");
        }
        
        log.info("=== END RESPONSE ===");
    }
    
    /**
     * Very simple endpoint to test if controller is loaded
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping(HttpServletRequest request) {
        logRequest(request, null);
        
        log.info("=== PING ENDPOINT CALLED ===");
        String response = "Guide controller is alive!";
        
        ResponseEntity<String> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Create a new guide profile for the authenticated user
     * Only users with GUIDE or ADMIN role can create guide profiles
     */
    @PostMapping
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> createGuideProfile(
            @Valid @RequestBody GuideProfileRequest request,
            HttpServletRequest httpRequest) {
        
        logRequest(httpRequest, request);
        log.info("=== GUIDE CONTROLLER: Creating guide profile ===");
        
        // Get user ID from JWT token
        Long userId = getCurrentUserId();
        log.info("Extracted user ID: {}", userId);
        
        GuideProfileResponse response = guideService.createGuideProfile(userId, request);
        log.info("Successfully created guide profile with ID: {}", response.getId());
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(response);
        logResponse(HttpStatus.CREATED, response);
        return responseEntity;
    }
    
    /**
     * Upgrade user to GUIDE role and create their first guide profile
     * This endpoint is for users who want to become guides
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<GuideProfileResponse> upgradeToGuideAndCreateProfile(
            @Valid @RequestBody GuideProfileRequest request,
            HttpServletRequest httpRequest) {
        
        logRequest(httpRequest, request);
        log.info("=== GUIDE CONTROLLER: Upgrading user to GUIDE and creating profile ===");
        
        // Get user ID from JWT token
        Long userId = getCurrentUserId();
        log.info("Extracted user ID: {}", userId);
        
        GuideProfileResponse response = guideService.upgradeToGuideAndCreateProfile(userId, request);
        log.info("Successfully upgraded user to GUIDE and created profile with ID: {}", response.getId());
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.status(HttpStatus.CREATED).body(response);
        logResponse(HttpStatus.CREATED, response);
        return responseEntity;
    }
    
    /**
     * Simple health check endpoint to verify controller is working
     */
    @GetMapping("/health")
    public ResponseEntity<String> health(HttpServletRequest request) {
        logRequest(request, null);
        
        log.info("Guide controller health check");
        String response = "Guide controller is working!";
        
        ResponseEntity<String> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Simple test endpoint to verify controller is accessible
     */
    @GetMapping("/test")
    public ResponseEntity<String> test(HttpServletRequest request) {
        logRequest(request, null);
        
        log.info("Guide controller test endpoint called");
        String response = "Guide controller test endpoint is working!";
        
        ResponseEntity<String> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Simple test endpoint to verify public access is working
     */
    @GetMapping("/test-public")
    public ResponseEntity<String> testPublic(HttpServletRequest request) {
        logRequest(request, null);
        
        log.info("=== PUBLIC TEST ENDPOINT CALLED ===");
        String response = "Public guide endpoint is working! No authentication required.";
        
        ResponseEntity<String> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Public endpoint to get all verified guides
     * No authentication required - accessible to everyone
     */
    @GetMapping("/verified")
    public ResponseEntity<List<GuideProfileResponse>> getAllVerifiedGuides(HttpServletRequest request) {
        logRequest(request, null);
        log.info("=== PUBLIC ENDPOINT: Fetching all verified guides ===");
        
        List<GuideProfileResponse> verifiedGuides = guideService.getAllVerifiedGuides();
        log.info("Successfully fetched {} verified guides", verifiedGuides.size());
        
        ResponseEntity<List<GuideProfileResponse>> responseEntity = ResponseEntity.ok(verifiedGuides);
        logResponse(HttpStatus.OK, verifiedGuides);
        return responseEntity;
    }
    
    /**
     * Public endpoint to get verified guides with pagination
     * No authentication required - accessible to everyone
     */
    @GetMapping("/verified/paginated")
    public ResponseEntity<Page<GuideProfileResponse>> getVerifiedGuidesPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            HttpServletRequest request) {
        
        logRequest(request, null, "page=" + page, "size=" + size, "sortBy=" + sortBy, "sortDir=" + sortDir);
        log.info("=== PUBLIC ENDPOINT: Fetching verified guides with pagination ===");
        
        // Create pageable object with sorting
        Sort sort = sortDir.equalsIgnoreCase("desc") 
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<GuideProfileResponse> verifiedGuides = guideService.getVerifiedGuidesPaginated(pageable);
        log.info("Successfully fetched {} verified guides (page {} of {})", 
                verifiedGuides.getContent().size(), page, verifiedGuides.getTotalPages());
        
        ResponseEntity<Page<GuideProfileResponse>> responseEntity = ResponseEntity.ok(verifiedGuides);
        logResponse(HttpStatus.OK, verifiedGuides);
        return responseEntity;
    }
    
    /**
     * Get guide profile by ID
     */
    @GetMapping("/{guideId}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<GuideProfileResponse> getGuideProfile(
            @PathVariable Long guideId,
            HttpServletRequest request) {
        
        logRequest(request, null, "guideId=" + guideId);
        log.info("Fetching guide profile with ID: {}", guideId);
        
        GuideProfileResponse response = guideService.getGuideProfile(guideId);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Get guide profile for the authenticated user
     */
    @GetMapping("/my-profile")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> getMyGuideProfile(HttpServletRequest request) {
        logRequest(request, null);
        log.info("Fetching guide profile for authenticated user");
        
        Long userId = getCurrentUserId();
        GuideProfileResponse response = guideService.getGuideProfileByUserId(userId);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Update guide profile for the authenticated user
     */
    @PutMapping("/my-profile")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> updateMyGuideProfile(
            @Valid @RequestBody GuideProfileRequest request,
            HttpServletRequest httpRequest) {
        
        logRequest(httpRequest, request);
        log.info("Updating guide profile for authenticated user");
        
        Long userId = getCurrentUserId();
        GuideProfileResponse currentProfile = guideService.getGuideProfileByUserId(userId);
        
        GuideProfileResponse response = guideService.updateGuideProfile(currentProfile.getId(), request);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Update guide profile by ID (admin only)
     */
    @PutMapping("/{guideId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> updateGuideProfile(
            @PathVariable Long guideId,
            @Valid @RequestBody GuideProfileRequest request,
            HttpServletRequest httpRequest) {
        
        logRequest(httpRequest, request, "guideId=" + guideId);
        log.info("Updating guide profile with ID: {}", guideId);
        
        GuideProfileResponse response = guideService.updateGuideProfile(guideId, request);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Delete guide profile for the authenticated user
     */
    @DeleteMapping("/my-profile")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMyGuideProfile(HttpServletRequest request) {
        logRequest(request, null);
        log.info("Deleting guide profile for authenticated user");
        
        Long userId = getCurrentUserId();
        GuideProfileResponse currentProfile = guideService.getGuideProfileByUserId(userId);
        
        guideService.deleteGuideProfile(currentProfile.getId());
        
        ResponseEntity<Void> responseEntity = ResponseEntity.noContent().build();
        logResponse(HttpStatus.NO_CONTENT, null);
        return responseEntity;
    }
    
    /**
     * Delete guide profile by ID (admin only)
     */
    @DeleteMapping("/{guideId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGuideProfile(
            @PathVariable Long guideId,
            HttpServletRequest request) {
        
        logRequest(request, null, "guideId=" + guideId);
        log.info("Deleting guide profile with ID: {}", guideId);
        
        guideService.deleteGuideProfile(guideId);
        
        ResponseEntity<Void> responseEntity = ResponseEntity.noContent().build();
        logResponse(HttpStatus.NO_CONTENT, null);
        return responseEntity;
    }
    
    /**
     * Get guides by verification status (admin only)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<GuideProfileResponse>> getGuidesByVerificationStatus(
            @RequestParam(required = false) String verificationStatus,
            HttpServletRequest request) {
        
        logRequest(request, null, "verificationStatus=" + verificationStatus);
        log.info("Fetching guides with verification status: {}", verificationStatus);
        
        List<GuideProfileResponse> guides;
        if ("PENDING".equals(verificationStatus)) {
            guides = guideService.getGuidesByVerificationStatus(Guide.VerificationStatus.PENDING);
        } else if ("VERIFIED".equals(verificationStatus)) {
            guides = guideService.getGuidesByVerificationStatus(Guide.VerificationStatus.VERIFIED);
        } else if ("REJECTED".equals(verificationStatus)) {
            guides = guideService.getGuidesByVerificationStatus(Guide.VerificationStatus.REJECTED);
        } else {
            // If no status specified, return all guides
            guides = guideService.getAllGuides();
        }
        
        ResponseEntity<List<GuideProfileResponse>> responseEntity = ResponseEntity.ok(guides);
        logResponse(HttpStatus.OK, guides);
        return responseEntity;
    }
    
    /**
     * Approve guide profile (admin only)
     */
    @PostMapping("/{guideId}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> verifyGuideProfile(
            @PathVariable Long guideId,
            HttpServletRequest request) {
        
        logRequest(request, null, "guideId=" + guideId);
        log.info("Verifying guide profile with ID: {}", guideId);
        
        GuideProfileResponse response = guideService.verifyGuideProfile(guideId);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Reject guide profile (admin only)
     */
    @PostMapping("/{guideId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> rejectGuideProfile(
            @PathVariable Long guideId,
            @RequestParam String reason,
            HttpServletRequest request) {
        
        logRequest(request, null, "guideId=" + guideId, "reason=" + reason);
        log.info("Rejecting guide profile with ID: {} for reason: {}", guideId, reason);
        
        GuideProfileResponse response = guideService.rejectGuideProfile(guideId, reason);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
        logResponse(HttpStatus.OK, response);
        return responseEntity;
    }
    
    /**
     * Get guide profile for the authenticated user if it exists
     * Returns the full guide profile information or 404 if not found
     */
    @GetMapping("/my-profile/exists")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> getMyGuideProfileIfExists(HttpServletRequest request) {
        logRequest(request, null);
        log.info("Fetching guide profile for authenticated user if it exists");
        
        Long userId = getCurrentUserId();
        
        try {
            GuideProfileResponse response = guideService.getGuideProfileByUserId(userId);
            log.info("Successfully found guide profile for user ID: {}", userId);
            
            ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(response);
            logResponse(HttpStatus.OK, response);
            return responseEntity;
        } catch (IllegalArgumentException e) {
            log.info("No guide profile found for user ID: {}", userId);
            ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.notFound().build();
            logResponse(HttpStatus.NOT_FOUND, null);
            return responseEntity;
        }
    }
    
    /**
     * Upload profile picture for the authenticated guide
     * Only guides can upload their profile pictures
     */
    @PostMapping("/my-profile/picture")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    public ResponseEntity<GuideProfileResponse> uploadGuideProfilePicture(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        
        logRequest(request, null);
        log.info("=== GUIDE CONTROLLER: Uploading guide profile picture ===");
        
        // Get user ID from JWT token
        Long userId = getCurrentUserId();
        log.info("Extracted user ID: {}", userId);
        
        // Check if user has a guide profile
        GuideProfileResponse currentProfile = guideService.getGuideProfileByUserId(userId);
        log.info("Found guide profile with ID: {}", currentProfile.getId());
        
        // Upload file to Cloudinary
        String imageUrl = fileUploadService.uploadProfilePicture(file, currentProfile.getUserEmail());
        log.info("Profile picture uploaded successfully: {}", imageUrl);
        
        // Update user's profile picture URL
        userService.updateProfilePictureByEmail(currentProfile.getUserEmail(), imageUrl);
        log.info("User profile picture updated successfully");
        
        // Return updated guide profile
        GuideProfileResponse updatedProfile = guideService.getGuideProfileByUserId(userId);
        
        ResponseEntity<GuideProfileResponse> responseEntity = ResponseEntity.ok(updatedProfile);
        logResponse(HttpStatus.OK, updatedProfile);
        return responseEntity;
    }
    
    /**
     * Get current user ID from authentication context
     */
    private Long getCurrentUserId() {
        log.info("=== EXTRACTING USER ID FROM AUTHENTICATION ===");
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("Authentication context is null or principal is null");
            throw new IllegalStateException("Authentication context is null or principal is null");
        }
        
        // Cast the principal to User entity (which implements UserDetails)
        if (authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            Long userId = user.getId();
            log.info("User ID extracted from authentication: {}", userId);
            return userId;
        } else {
            log.error("Authentication principal is not a User entity: {}", 
                    authentication.getPrincipal().getClass().getName());
            throw new IllegalStateException("Authentication principal is not a User entity");
        }
    }
}
