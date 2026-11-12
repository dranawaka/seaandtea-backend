package com.seaandtea.repository;

import com.seaandtea.entity.Tour;
import com.seaandtea.entity.Tour.TourCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TourRepositoryCustom {

    Page<Tour> searchMarketplace(
            TourCategory category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDuration,
            Integer maxDuration,
            String searchTerm,
            String destination,
            Boolean instantBooking,
            String sortBy,
            String sortDirection,
            Pageable pageable
    );
}
