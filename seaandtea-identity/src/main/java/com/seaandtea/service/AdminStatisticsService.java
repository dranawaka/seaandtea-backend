package com.seaandtea.service;

import com.seaandtea.dto.AdminStatisticsResponse;
import com.seaandtea.entity.Guide;
import com.seaandtea.repository.BookingRepository;
import com.seaandtea.repository.GuideRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final GuideRepository guideRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public AdminStatisticsResponse getStatistics() {
        return AdminStatisticsResponse.builder()
                .totalUsers(userRepository.count())
                .verifiedGuides(guideRepository.countByVerificationStatus(Guide.VerificationStatus.VERIFIED))
                .pendingGuides(guideRepository.countByVerificationStatus(Guide.VerificationStatus.PENDING))
                .totalBookings(bookingRepository.count())
                .build();
    }
}
