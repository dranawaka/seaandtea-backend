package com.seaandtea.repository;

import com.seaandtea.entity.Tour;
import com.seaandtea.entity.Tour.TourCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Repository
public class TourRepositoryImpl implements TourRepositoryCustom {

    private final EntityManager entityManager;

    public TourRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Tour> searchMarketplace(
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
    ) {
        StringBuilder where = new StringBuilder(" WHERE t.isActive = true");
        Map<String, Object> params = new LinkedHashMap<>();

        if (category != null) {
            where.append(" AND t.category = :category");
            params.put("category", category);
        }
        if (minPrice != null) {
            where.append(" AND t.pricePerPerson >= :minPrice");
            params.put("minPrice", minPrice);
        }
        if (maxPrice != null) {
            where.append(" AND t.pricePerPerson <= :maxPrice");
            params.put("maxPrice", maxPrice);
        }
        if (minDuration != null) {
            where.append(" AND t.durationHours >= :minDuration");
            params.put("minDuration", minDuration);
        }
        if (maxDuration != null) {
            where.append(" AND t.durationHours <= :maxDuration");
            params.put("maxDuration", maxDuration);
        }
        if (instantBooking != null) {
            where.append(" AND t.instantBooking = :instantBooking");
            params.put("instantBooking", instantBooking);
        }
        if (destination != null && !destination.isBlank()) {
            where.append(" AND LOWER(t.destination) = LOWER(:destination)");
            params.put("destination", destination.trim());
        }
        if (searchTerm != null && !searchTerm.isBlank()) {
            where.append(" AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
                    .append(" OR LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
                    .append(" OR LOWER(COALESCE(t.destination, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
                    .append(" OR LOWER(COALESCE(t.meetingPoint, '')) LIKE LOWER(CONCAT('%', :searchTerm, '%')))");
            params.put("searchTerm", searchTerm.trim());
        }

        String dataJpql = "SELECT t FROM Tour t" + where + orderClause(sortBy, sortDirection);
        String countJpql = "SELECT COUNT(t) FROM Tour t" + where;

        TypedQuery<Tour> dataQuery = entityManager.createQuery(dataJpql, Tour.class);
        bind(dataQuery, params);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<Tour> content = dataQuery.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);
        bind(countQuery, params);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private static void bind(TypedQuery<?> query, Map<String, Object> params) {
        params.forEach(query::setParameter);
    }

    private static String orderClause(String sortBy, String sortDirection) {
        String direction = "asc".equalsIgnoreCase(sortDirection) ? "ASC" : "DESC";
        String field = sortBy == null ? "createdat" : sortBy.toLowerCase(Locale.ROOT);
        return switch (field) {
            case "price", "priceperperson" -> " ORDER BY t.pricePerPerson " + direction + ", t.id DESC";
            case "rating", "averagerating" ->
                    " ORDER BY (SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.tour.id = t.id) " + direction + ", t.id DESC";
            case "bookings", "totalbookings", "popular" ->
                    " ORDER BY (SELECT COUNT(b.id) FROM Booking b WHERE b.tour.id = t.id) " + direction + ", t.id DESC";
            default -> " ORDER BY t.createdAt " + direction + ", t.id DESC";
        };
    }
}
