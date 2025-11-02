package com.seaandtea.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourImageUploadRequest {
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private Boolean isPrimary = false;
    
    @Size(max = 200, message = "Alt text cannot exceed 200 characters")
    private String altText;
}


