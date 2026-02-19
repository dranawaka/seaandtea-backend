package com.seaandtea.controller;

import com.seaandtea.dto.HomepageSliderImageResponse;
import com.seaandtea.dto.ProductResponse;
import com.seaandtea.dto.TourImageUploadRequest;
import com.seaandtea.dto.TourResponse;
import com.seaandtea.dto.UserDto;
import com.seaandtea.service.FileUploadService;
import com.seaandtea.service.HomepageSliderService;
import com.seaandtea.service.ProductService;
import com.seaandtea.service.TourService;
import com.seaandtea.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "File Upload", description = "API endpoints for file upload operations")
public class FileUploadController {
    
    private final FileUploadService fileUploadService;
    private final TourService tourService;
    private final UserService userService;
    private final ProductService productService;
    private final HomepageSliderService homepageSliderService;
    
    @PostMapping("/tour/{tourId}/image")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Upload tour image",
        description = "Uploads an image file for a tour and returns the image URL",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = TourResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file or request data"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Cannot modify this tour"),
        @ApiResponse(responseCode = "404", description = "Tour not found"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<TourResponse> uploadTourImage(
            @PathVariable Long tourId,
            
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            
            @Parameter(description = "Whether this should be the primary image")
            @RequestParam(defaultValue = "false") Boolean isPrimary,
            
            @Parameter(description = "Alt text for the image")
            @RequestParam(required = false) String altText,
            
            Authentication authentication) {
        
        log.info("Uploading image for tour: {} by user: {}", tourId, authentication.getName());
        
        // Upload file to S3
        String imageUrl = fileUploadService.uploadTourImage(file, tourId, authentication.getName());
        
        // Create tour image record
        TourImageUploadRequest request = TourImageUploadRequest.builder()
            .imageUrl(imageUrl)
            .isPrimary(isPrimary)
            .altText(altText)
            .build();
        
        TourResponse response = tourService.addImageToTour(tourId, request, authentication.getName());
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/product/{productId}/image")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Upload product image",
        description = "Uploads an image for a product and adds it to the product's images. Returns the full product with updated images.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file or product already has 10 images"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<ProductResponse> uploadProductImage(
            @PathVariable Long productId,
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Whether this should be the primary image")
            @RequestParam(defaultValue = "false") Boolean isPrimary,
            @Parameter(description = "Alt text for the image")
            @RequestParam(required = false) String altText,
            Authentication authentication) {

        log.info("Uploading image for product: {} by user: {}", productId, authentication.getName());
        String imageUrl = fileUploadService.uploadProductImage(file, productId, authentication.getName());
        ProductResponse response = productService.addImageToProduct(productId, imageUrl, isPrimary, altText);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/profile-picture")
    @PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Upload profile picture",
        description = "Uploads a profile picture for the authenticated user and updates their profile",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profile picture uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<UserDto> uploadProfilePicture(
            @Parameter(description = "Profile picture file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            
            Authentication authentication) {
        
        log.info("Uploading profile picture for user: {}", authentication.getName());
        
        // Upload file to Cloudinary
        String imageUrl = fileUploadService.uploadProfilePicture(file, authentication.getName());
        
        // Get user by email and update profile picture URL
        UserDto updatedUser = userService.updateProfilePictureByEmail(authentication.getName(), imageUrl);
        
        return ResponseEntity.ok(updatedUser);
    }
    
    @PostMapping("/guide-profile-picture")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Upload guide profile picture",
        description = "Uploads a profile picture specifically for guide profiles and updates both user and guide profile",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Guide profile picture uploaded successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - User must be a GUIDE"),
        @ApiResponse(responseCode = "404", description = "Guide profile not found"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<UserDto> uploadGuideProfilePicture(
            @Parameter(description = "Guide profile picture file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            
            Authentication authentication) {
        
        log.info("Uploading guide profile picture for user: {}", authentication.getName());
        
        // Upload file to Cloudinary
        String imageUrl = fileUploadService.uploadProfilePicture(file, authentication.getName());
        
        // Get user by email and update profile picture URL
        UserDto updatedUser = userService.updateProfilePictureByEmail(authentication.getName(), imageUrl);
        
        return ResponseEntity.ok(updatedUser);
    }

    // ---------- Homepage slider (admin-managed) ----------

    @GetMapping("/homepage-slider")
    @Operation(
        summary = "List homepage slider images",
        description = "Returns all homepage slider images in display order. Public endpoint; no authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of slider images",
                    content = @Content(schema = @Schema(implementation = HomepageSliderImageResponse.class)))
    })
    public ResponseEntity<List<HomepageSliderImageResponse>> getHomepageSliderImages() {
        List<HomepageSliderImageResponse> images = homepageSliderService.getAll();
        return ResponseEntity.ok(images);
    }

    @PostMapping("/homepage-slider")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Upload homepage slider image",
        description = "Uploads an image for the homepage slider. ADMIN only.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slider image uploaded successfully",
                    content = @Content(schema = @Schema(implementation = HomepageSliderImageResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only"),
        @ApiResponse(responseCode = "413", description = "File too large")
    })
    public ResponseEntity<HomepageSliderImageResponse> uploadHomepageSliderImage(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Display order (optional; defaults to end)")
            @RequestParam(required = false) Integer sortOrder,
            @Parameter(description = "Alt text for the image")
            @RequestParam(required = false) String altText,
            Authentication authentication) {
        log.info("Uploading homepage slider image by admin: {}", authentication.getName());
        String imageUrl = fileUploadService.uploadHomepageSliderImage(file, authentication.getName());
        HomepageSliderImageResponse response = homepageSliderService.add(imageUrl, sortOrder, altText);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/homepage-slider/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete homepage slider image",
        description = "Deletes a homepage slider image by ID and removes it from Cloudinary. ADMIN only.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Slider image deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - ADMIN only"),
        @ApiResponse(responseCode = "404", description = "Slider image not found")
    })
    public ResponseEntity<Map<String, String>> deleteHomepageSliderImage(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Deleting homepage slider image id={} by admin: {}", id, authentication.getName());
        homepageSliderService.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Homepage slider image deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/image")
    @PreAuthorize("hasRole('GUIDE') or hasRole('ADMIN')")
    @Operation(
        summary = "Delete image",
        description = "Deletes an image from S3 storage",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid image URL"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<Map<String, String>> deleteImage(
            @Parameter(description = "Image URL to delete", required = true)
            @RequestParam String imageUrl,
            
            Authentication authentication) {
        
        log.info("Deleting image: {} by user: {}", imageUrl, authentication.getName());
        
        fileUploadService.deleteImage(imageUrl);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Image deleted successfully");
        
        return ResponseEntity.ok(response);
    }
}


