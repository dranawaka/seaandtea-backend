package com.seaandtea.controller;

import com.seaandtea.dto.*;
import com.seaandtea.entity.Tour.TourCategory;
import com.seaandtea.service.TourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tour Management", description = "API endpoints for managing tours")
public class TourController {
    
    private final TourService tourService;
    
    @PostMapping
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Create a new tour",
        description = "Creates a new tour for the authenticated guide",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tour created successfully",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only guides can create tours")
    })
    public ResponseEntity<TourResponse> createTour(
            @Valid @RequestBody TourCreateRequest request,
            Authentication authentication) {
        
        log.info("Creating tour: {}", request.getTitle());
        TourResponse response = tourService.createTour(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Update a tour",
        description = "Updates an existing tour. Guide can only update their own tours, admins can update any tour.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tour updated successfully",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot update this tour"),
        @ApiResponse(responseCode = "404", description = "Tour not found")
    })
    public ResponseEntity<TourResponse> updateTour(
            @PathVariable Long id,
            @Valid @RequestBody TourUpdateRequest request,
            Authentication authentication) {
        
        log.info("Updating tour: {}", id);
        TourResponse response = tourService.updateTour(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(
        summary = "Get tour by ID",
        description = "Retrieves a tour by its ID including guide information and images"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tour found",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tour not found")
    })
    public ResponseEntity<TourResponse> getTourById(@PathVariable Long id) {
        log.info("Getting tour by ID: {}", id);
        TourResponse response = tourService.getTourById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @Operation(
        summary = "Get tours with filters",
        description = "Retrieves tours with optional filtering and pagination"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tours retrieved successfully")
    })
    public ResponseEntity<Page<TourListResponse>> getTours(
            @Parameter(description = "Search term for title or description")
            @RequestParam(required = false) String searchTerm,
            
            @Parameter(description = "Filter by category")
            @RequestParam(required = false) TourCategory category,
            
            @Parameter(description = "Minimum price filter")
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            
            @Parameter(description = "Maximum price filter")
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            
            @Parameter(description = "Minimum duration in hours")
            @RequestParam(required = false) Integer minDuration,
            
            @Parameter(description = "Maximum duration in hours")
            @RequestParam(required = false) Integer maxDuration,
            
            @Parameter(description = "Filter by instant booking availability")
            @RequestParam(required = false) Boolean instantBooking,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        TourFilterRequest filterRequest = TourFilterRequest.builder()
            .searchTerm(searchTerm)
            .category(category)
            .minPrice(minPrice)
            .maxPrice(maxPrice)
            .minDuration(minDuration)
            .maxDuration(maxDuration)
            .instantBooking(instantBooking)
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();
        
        Page<TourListResponse> response = tourService.getToursWithFilters(filterRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/guide/{guideId}")
    @Operation(
        summary = "Get tours by guide",
        description = "Retrieves all active tours for a specific guide"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tours retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Guide not found")
    })
    public ResponseEntity<List<TourResponse>> getToursByGuide(@PathVariable Long guideId) {
        log.info("Getting tours for guide: {}", guideId);
        List<TourResponse> response = tourService.getToursByGuide(guideId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/my-tours")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Get my tours",
        description = "Retrieves all tours (including inactive) for the authenticated guide",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tours retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Only guides can access this endpoint"),
        @ApiResponse(responseCode = "404", description = "Guide profile not found")
    })
    public ResponseEntity<List<TourResponse>> getMyTours(Authentication authentication) {
        log.info("Getting tours for authenticated guide: {}", authentication.getName());
        List<TourResponse> response = tourService.getMyTours(authentication.getName());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Delete a tour",
        description = "Soft deletes a tour (marks as inactive). Guide can only delete their own tours, admins can delete any tour.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Tour deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot delete this tour"),
        @ApiResponse(responseCode = "404", description = "Tour not found")
    })
    public ResponseEntity<Void> deleteTour(
            @PathVariable Long id,
            Authentication authentication) {
        
        log.info("Deleting tour: {}", id);
        tourService.deleteTour(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Add image to tour",
        description = "Adds an image to a tour. Guide can only modify their own tours, admins can modify any tour.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image added successfully",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot modify this tour"),
        @ApiResponse(responseCode = "404", description = "Tour not found")
    })
    public ResponseEntity<TourResponse> addImageToTour(
            @PathVariable Long id,
            @Valid @RequestBody TourImageUploadRequest request,
            Authentication authentication) {
        
        log.info("Adding image to tour: {}", id);
        TourResponse response = tourService.addImageToTour(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{tourId}/images/{imageId}")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Remove image from tour",
        description = "Removes an image from a tour. Guide can only modify their own tours, admins can modify any tour.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Image removed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot modify this tour"),
        @ApiResponse(responseCode = "404", description = "Tour or image not found")
    })
    public ResponseEntity<Void> removeImageFromTour(
            @PathVariable Long tourId,
            @PathVariable Long imageId,
            Authentication authentication) {
        
        log.info("Removing image {} from tour: {}", imageId, tourId);
        tourService.removeImageFromTour(tourId, imageId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/popular")
    @Operation(
        summary = "Get popular tours",
        description = "Retrieves the most popular tours based on booking count"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Popular tours retrieved successfully")
    })
    public ResponseEntity<Page<TourListResponse>> getPopularTours(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        Page<TourListResponse> response = tourService.getPopularTours(page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{category}")
    @Operation(
        summary = "Get tours by category",
        description = "Retrieves tours filtered by category"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tours retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid category")
    })
    public ResponseEntity<Page<TourListResponse>> getToursByCategory(
            @PathVariable TourCategory category,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size) {
        
        Page<TourListResponse> response = tourService.getToursByCategory(category, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get tour statistics",
        description = "Retrieves tour statistics including counts by category (Admin only)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    })
    public ResponseEntity<Map<String, Object>> getTourStatistics() {
        Map<String, Object> response = tourService.getTourStatistics();
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/public/verified")
    @Operation(
        summary = "Get all verified tours (Public)",
        description = "Retrieves all active tours from verified guides. This is a public endpoint that doesn't require authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verified tours retrieved successfully")
    })
    public ResponseEntity<List<TourResponse>> getPublicVerifiedTours() {
        log.info("=== PUBLIC ENDPOINT: Fetching all verified tours ===");
        List<TourResponse> verifiedTours = tourService.getAllVerifiedTours();
        log.info("Successfully fetched {} verified tours", verifiedTours.size());
        return ResponseEntity.ok(verifiedTours);
    }
    
    @GetMapping("/public/verified/paginated")
    @Operation(
        summary = "Get verified tours with pagination (Public)",
        description = "Retrieves active tours from verified guides with pagination. This is a public endpoint that doesn't require authentication."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Verified tours retrieved successfully")
    })
    public ResponseEntity<Page<TourListResponse>> getPublicVerifiedToursPaginated(
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "10") int size,
            
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction (asc/desc)")
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        log.info("=== PUBLIC ENDPOINT: Fetching verified tours with pagination ===");
        
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
            org.springframework.data.domain.Sort.Direction.fromString(sortDirection), sortBy);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        Page<TourListResponse> verifiedTours = tourService.getVerifiedTours(pageable);
        log.info("Successfully fetched {} verified tours (page {} of {})",
                verifiedTours.getContent().size(), page, verifiedTours.getTotalPages());
        
        return ResponseEntity.ok(verifiedTours);
    }
}


