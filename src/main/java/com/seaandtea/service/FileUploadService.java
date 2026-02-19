package com.seaandtea.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {
    
    private final Cloudinary cloudinary;
    
    @Value("${cloudinary.folder:seaandtea}")
    private String baseFolder;
    
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );
    
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    public String uploadTourImage(MultipartFile file, Long tourId, String uploaderEmail) {
        validateImageFile(file);
        
        try {
            String publicId = generateTourImagePublicId(tourId);
            String folder = baseFolder + "/tours/" + tourId + "/images";
            
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "format", "webp", // Auto-convert to WebP for optimization
                "quality", "auto:good", // Automatic quality optimization
                "fetch_format", "auto", // Auto-format selection based on browser support
                "width", 1200,
                "height", 800,
                "crop", "limit" // Don't upscale, only downscale if larger
            );
            
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = (String) uploadResult.get("secure_url");
            
            log.info("Image uploaded successfully to Cloudinary: {} by user: {}", imageUrl, uploaderEmail);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Error uploading image for tour: {}", tourId, e);
            throw new RuntimeException("Failed to upload image");
        }
    }
    
    public String uploadProfilePicture(MultipartFile file, String userEmail) {
        validateImageFile(file);
        
        try {
            String publicId = generateProfilePublicId(userEmail);
            String folder = baseFolder + "/profiles";
            
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "format", "webp", // Auto-convert to WebP for optimization
                "quality", "auto:good",
                "fetch_format", "auto",
                "width", 400,
                "height", 400,
                "crop", "fill", // Crop to fit the dimensions
                "gravity", "face" // Focus on face if detected
            );
            
            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = (String) uploadResult.get("secure_url");
            
            log.info("Profile picture uploaded successfully to Cloudinary: {} for user: {}", imageUrl, userEmail);
            return imageUrl;
            
        } catch (IOException e) {
            log.error("Error uploading profile picture for user: {}", userEmail, e);
            throw new RuntimeException("Failed to upload profile picture");
        }
    }

    public String uploadHomepageSliderImage(MultipartFile file, String uploaderEmail) {
        validateImageFile(file);

        try {
            String publicId = generateSliderImagePublicId();
            String folder = baseFolder + "/homepage/slider";

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "format", "webp",
                "quality", "auto:good",
                "fetch_format", "auto",
                "width", 1920,
                "height", 720,
                "crop", "limit"
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = (String) uploadResult.get("secure_url");

            log.info("Homepage slider image uploaded to Cloudinary: {} by user: {}", imageUrl, uploaderEmail);
            return imageUrl;

        } catch (IOException e) {
            log.error("Error uploading homepage slider image", e);
            throw new RuntimeException("Failed to upload homepage slider image");
        }
    }
    
    public void deleteImage(String imageUrl) {
        try {
            String publicId = extractPublicIdFromUrl(imageUrl);
            
            Map<String, Object> deleteParams = ObjectUtils.asMap(
                "resource_type", "image"
            );
            
            Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId, deleteParams);
            String result = (String) deleteResult.get("result");
            
            if ("ok".equals(result)) {
                log.info("Image deleted successfully from Cloudinary: {}", imageUrl);
            } else {
                log.warn("Image deletion result: {} for URL: {}", result, imageUrl);
            }
            
        } catch (Exception e) {
            log.error("Error deleting image from Cloudinary: {}", imageUrl, e);
            throw new RuntimeException("Failed to delete image");
        }
    }
    
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
        
        String contentType = file.getContentType();
        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("File type not supported. Allowed types: JPEG, PNG, WebP");
        }
    }
    
    private String generateTourImagePublicId(Long tourId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("tour_%d_%s_%s", tourId, timestamp, uuid);
    }
    
    private String generateProfilePublicId(String userEmail) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String emailHash = String.valueOf(userEmail.hashCode()).replace("-", "");
        return String.format("profile_%s_%s", emailHash, timestamp);
    }

    private String generateSliderImagePublicId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("slider_%s_%s", timestamp, uuid);
    }

    public String uploadProductImage(MultipartFile file, Long productId, String uploaderEmail) {
        validateImageFile(file);

        try {
            String publicId = generateProductImagePublicId(productId);
            String folder = baseFolder + "/products/" + productId + "/images";

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "public_id", publicId,
                "folder", folder,
                "resource_type", "image",
                "format", "webp",
                "quality", "auto:good",
                "fetch_format", "auto",
                "width", 1200,
                "height", 1200,
                "crop", "limit"
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String imageUrl = (String) uploadResult.get("secure_url");

            log.info("Product image uploaded to Cloudinary: {} for product {} by user: {}", imageUrl, productId, uploaderEmail);
            return imageUrl;

        } catch (IOException e) {
            log.error("Error uploading image for product: {}", productId, e);
            throw new RuntimeException("Failed to upload product image");
        }
    }

    private String generateProductImagePublicId(Long productId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return String.format("product_%d_%s_%s", productId, timestamp, uuid);
    }
    
    private String extractPublicIdFromUrl(String imageUrl) {
        // Extract Cloudinary public_id from URL
        // Example: https://res.cloudinary.com/cloud/image/upload/v123456/folder/public_id.format
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String afterUpload = parts[1];
                // Remove version (v123456/) if present
                if (afterUpload.startsWith("v")) {
                    int slashIndex = afterUpload.indexOf("/");
                    if (slashIndex > 0) {
                        afterUpload = afterUpload.substring(slashIndex + 1);
                    }
                }
                // Remove file extension
                int dotIndex = afterUpload.lastIndexOf(".");
                if (dotIndex > 0) {
                    afterUpload = afterUpload.substring(0, dotIndex);
                }
                return afterUpload;
            }
        } catch (Exception e) {
            log.warn("Could not extract public_id from URL: {}", imageUrl);
        }
        throw new IllegalArgumentException("Invalid Cloudinary URL format: " + imageUrl);
    }
}

