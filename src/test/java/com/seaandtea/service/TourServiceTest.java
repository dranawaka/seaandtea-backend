package com.seaandtea.service;

import com.seaandtea.dto.TourListResponse;
import com.seaandtea.dto.TourResponse;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.User;
import com.seaandtea.entity.Tour.TourCategory;
import com.seaandtea.entity.Guide.VerificationStatus;
import com.seaandtea.repository.TourRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepository;

    @InjectMocks
    private TourService tourService;

    private User testUser;
    private Guide testGuide;
    private Tour testTour;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .isVerified(true)
                .build();

        testGuide = Guide.builder()
                .id(1L)
                .user(testUser)
                .bio("Experienced tour guide")
                .verificationStatus(VerificationStatus.VERIFIED)
                .averageRating(BigDecimal.valueOf(4.5))
                .totalTours(10)
                .build();

        testTour = Tour.builder()
                .id(1L)
                .guide(testGuide)
                .title("Amazing Tea Tour")
                .description("Experience the best tea tours in the region")
                .category(TourCategory.TEA_TOURS)
                .durationHours(3)
                .maxGroupSize(8)
                .pricePerPerson(BigDecimal.valueOf(50.00))
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getVerifiedTours_ShouldReturnVerifiedTours() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tour> tourPage = new PageImpl<>(Arrays.asList(testTour), pageable, 1);
        
        when(tourRepository.findActiveToursFromVerifiedGuidesWithImages(pageable))
                .thenReturn(tourPage);

        // Act
        Page<TourListResponse> result = tourService.getVerifiedTours(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        TourListResponse tourResponse = result.getContent().get(0);
        assertEquals(testTour.getId(), tourResponse.getId());
        assertEquals(testTour.getTitle(), tourResponse.getTitle());
        assertEquals(testTour.getGuide().getUser().getFirstName() + " " + 
                   testTour.getGuide().getUser().getLastName(), tourResponse.getGuideName());
        
        verify(tourRepository).findActiveToursFromVerifiedGuidesWithImages(pageable);
    }

    @Test
    void getAllVerifiedTours_ShouldReturnAllVerifiedTours() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1000);
        Page<Tour> tourPage = new PageImpl<>(Arrays.asList(testTour), pageable, 1);
        
        when(tourRepository.findActiveToursFromVerifiedGuidesWithImages(pageable))
                .thenReturn(tourPage);

        // Act
        List<TourResponse> result = tourService.getAllVerifiedTours();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        
        TourResponse tourResponse = result.get(0);
        assertEquals(testTour.getId(), tourResponse.getId());
        assertEquals(testTour.getTitle(), tourResponse.getTitle());
        assertEquals(testTour.getDescription(), tourResponse.getDescription());
        assertEquals(testTour.getCategory(), tourResponse.getCategory());
        
        verify(tourRepository).findActiveToursFromVerifiedGuidesWithImages(pageable);
    }

    @Test
    void getVerifiedTours_WithEmptyResult_ShouldReturnEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tour> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);
        
        when(tourRepository.findActiveToursFromVerifiedGuidesWithImages(pageable))
                .thenReturn(emptyPage);

        // Act
        Page<TourListResponse> result = tourService.getVerifiedTours(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());
        
        verify(tourRepository).findActiveToursFromVerifiedGuidesWithImages(pageable);
    }
}
