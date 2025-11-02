package com.seaandtea.dto;

import com.seaandtea.entity.Tour.TourCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TourCreateRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;
    
    @NotNull(message = "Category is required")
    private TourCategory category;
    
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    @Max(value = 168, message = "Duration cannot exceed 168 hours (1 week)")
    private Integer durationHours;
    
    @Min(value = 1, message = "Maximum group size must be at least 1")
    @Max(value = 50, message = "Maximum group size cannot exceed 50")
    private Integer maxGroupSize = 10;
    
    @NotNull(message = "Price per person is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @DecimalMax(value = "10000.00", message = "Price cannot exceed 10,000")
    private BigDecimal pricePerPerson;
    
    private Boolean instantBooking = false;
    
    private Boolean securePayment = true;
    
    @Size(max = 10, message = "Maximum 10 languages allowed")
    private List<String> languages;
    
    @Size(max = 20, message = "Maximum 20 highlights allowed")
    private List<String> highlights;
    
    @Size(max = 30, message = "Maximum 30 included items allowed")
    private List<String> includedItems;
    
    @Size(max = 30, message = "Maximum 30 excluded items allowed")
    private List<String> excludedItems;
    
    @Size(max = 500, message = "Meeting point cannot exceed 500 characters")
    private String meetingPoint;
    
    @Size(max = 1000, message = "Cancellation policy cannot exceed 1000 characters")
    private String cancellationPolicy;
    
    @Size(max = 10, message = "Maximum 10 images allowed")
    private List<String> imageUrls;
    
    private Integer primaryImageIndex = 0;
}


