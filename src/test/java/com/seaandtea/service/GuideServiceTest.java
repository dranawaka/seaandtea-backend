package com.seaandtea.service;

import com.seaandtea.dto.GuideProfileRequest;
import com.seaandtea.dto.GuideProfileResponse;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.User;
import com.seaandtea.repository.GuideRepository;
import com.seaandtea.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuideServiceTest {
    
    @Mock
    private GuideRepository guideRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserService userService;
    
    @InjectMocks
    private GuideService guideService;
    
    private User testUser;
    private GuideProfileRequest testRequest;
    
    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .phone("+1234567890")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationality("US")
                .role(User.UserRole.USER)
                .build();
        
        testRequest = GuideProfileRequest.builder()
                .bio("Experienced tour guide with 5 years of experience in cultural tours and adventure activities.")
                .hourlyRate(new BigDecimal("25.00"))
                .dailyRate(new BigDecimal("200.00"))
                .responseTimeHours(24)
                .isAvailable(true)
                .specialties(Arrays.asList(
                        GuideProfileRequest.GuideSpecialtyRequest.builder()
                                .specialty("Cultural Tours")
                                .yearsExperience(5)
                                .build(),
                        GuideProfileRequest.GuideSpecialtyRequest.builder()
                                .specialty("Adventure")
                                .yearsExperience(3)
                                .build()
                ))
                .languages(Arrays.asList(
                        GuideProfileRequest.GuideLanguageRequest.builder()
                                .language("English")
                                .proficiencyLevel("NATIVE")
                                .build(),
                        GuideProfileRequest.GuideLanguageRequest.builder()
                                .language("Spanish")
                                .proficiencyLevel("FLUENT")
                                .build()
                ))
                .build();
    }
    
    @Test
    void createGuideProfile_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(guideRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(guideRepository.save(any(Guide.class))).thenAnswer(invocation -> {
            Guide guide = invocation.getArgument(0);
            guide.setId(1L);
            guide.setCreatedAt(LocalDateTime.now());
            guide.setUpdatedAt(LocalDateTime.now());
            return guide;
        });
        
        // When
        GuideProfileResponse response = guideService.createGuideProfile(1L, testRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("John", response.getUserFirstName());
        assertEquals("Doe", response.getUserLastName());
        assertEquals("test@example.com", response.getUserEmail());
        assertEquals(testRequest.getBio(), response.getBio());
        assertEquals(testRequest.getHourlyRate(), response.getHourlyRate());
        assertEquals(testRequest.getDailyRate(), response.getDailyRate());
        assertEquals(testRequest.getResponseTimeHours(), response.getResponseTimeHours());
        assertEquals(testRequest.getIsAvailable(), response.getIsAvailable());
        assertEquals(2, response.getSpecialties().size());
        assertEquals(2, response.getLanguages().size());
        
        // Note: createGuideProfile no longer updates user role, it assumes user already has GUIDE role
        verify(userService, never()).updateUserRole(any(), any());
        verify(guideRepository, times(2)).save(any(Guide.class));
    }
    
    @Test
    void upgradeToGuideAndCreateProfile_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(guideRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(guideRepository.save(any(Guide.class))).thenAnswer(invocation -> {
            Guide guide = invocation.getArgument(0);
            guide.setId(1L);
            guide.setCreatedAt(LocalDateTime.now());
            guide.setUpdatedAt(LocalDateTime.now());
            return guide;
        });
        
        // When
        GuideProfileResponse response = guideService.upgradeToGuideAndCreateProfile(1L, testRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
        assertEquals("John", response.getUserFirstName());
        assertEquals("Doe", response.getUserLastName());
        assertEquals("test@example.com", response.getUserEmail());
        assertEquals(testRequest.getBio(), response.getBio());
        assertEquals(testRequest.getHourlyRate(), response.getHourlyRate());
        assertEquals(testRequest.getDailyRate(), response.getDailyRate());
        assertEquals(testRequest.getResponseTimeHours(), response.getResponseTimeHours());
        assertEquals(testRequest.getIsAvailable(), response.getIsAvailable());
        assertEquals(2, response.getSpecialties().size());
        assertEquals(2, response.getLanguages().size());
        
        // Verify that user role was updated to GUIDE
        verify(userService).updateUserRole(1L, any());
        verify(guideRepository, times(2)).save(any(Guide.class));
    }
    
    @Test
    void createGuideProfile_UserAlreadyHasProfile_ThrowsException() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(guideRepository.findByUserId(1L)).thenReturn(Optional.of(new Guide()));
        
        // When & Then
        assertThrows(IllegalStateException.class, () -> 
                guideService.createGuideProfile(1L, testRequest));
        
        verify(guideRepository, never()).save(any(Guide.class));
    }
    
    @Test
    void getGuideProfile_Success() {
        // Given
        Guide guide = Guide.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .hourlyRate(new BigDecimal("25.00"))
                .dailyRate(new BigDecimal("200.00"))
                .responseTimeHours(24)
                .isAvailable(true)
                .verificationStatus(Guide.VerificationStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        when(guideRepository.findById(1L)).thenReturn(Optional.of(guide));
        
        // When
        GuideProfileResponse response = guideService.getGuideProfile(1L);
        
        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(1L, response.getUserId());
    }
    
    @Test
    void getGuideProfile_NotFound_ThrowsException() {
        // Given
        when(guideRepository.findById(1L)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
                guideService.getGuideProfile(1L));
    }
    
    @Test
    void deleteGuideProfile_Success() {
        // Given
        Guide testGuide = Guide.builder()
                .id(1L)
                .user(testUser)
                .bio("Test bio")
                .build();
        
        when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
        when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);
        
        // When
        guideService.deleteGuideProfile(1L);
        
        // Then
        verify(guideRepository).deleteById(1L);
    }
    
    @Test
    void getAllVerifiedGuides_Success() {
        // Given
        Guide verifiedGuide1 = Guide.builder()
                .id(1L)
                .user(testUser)
                .verificationStatus(Guide.VerificationStatus.VERIFIED)
                .bio("Verified guide 1")
                .build();
        
        Guide verifiedGuide2 = Guide.builder()
                .id(2L)
                .user(testUser)
                .verificationStatus(Guide.VerificationStatus.VERIFIED)
                .bio("Verified guide 2")
                .build();
        
        when(guideRepository.findByVerificationStatus(Guide.VerificationStatus.VERIFIED))
                .thenReturn(Arrays.asList(verifiedGuide1, verifiedGuide2));
        
        // When
        List<GuideProfileResponse> result = guideService.getAllVerifiedGuides();
        
        // Then
        assertEquals(2, result.size());
        verify(guideRepository).findByVerificationStatus(Guide.VerificationStatus.VERIFIED);
    }
    
    @Test
    void getVerifiedGuidesPaginated_Success() {
        // Given
        Guide verifiedGuide = Guide.builder()
                .id(1L)
                .user(testUser)
                .verificationStatus(Guide.VerificationStatus.VERIFIED)
                .bio("Verified guide")
                .build();
        
        org.springframework.data.domain.Page<Guide> page = mock(org.springframework.data.domain.Page.class);
        when(page.getContent()).thenReturn(Arrays.asList(verifiedGuide));
        when(page.getTotalPages()).thenReturn(1);
        when(page.getTotalElements()).thenReturn(1L);
        
        when(guideRepository.findByVerificationStatus(eq(Guide.VerificationStatus.VERIFIED), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(page);
        
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(0, 10);
        
        // When
        org.springframework.data.domain.Page<GuideProfileResponse> result = guideService.getVerifiedGuidesPaginated(pageable);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(guideRepository).findByVerificationStatus(Guide.VerificationStatus.VERIFIED, pageable);
    }
}

