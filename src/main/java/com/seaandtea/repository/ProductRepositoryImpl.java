package com.seaandtea.repository;

import com.seaandtea.entity.Product;
import com.seaandtea.entity.Product.ProductCategory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.hibernate.query.TypedParameterValue;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private static final String BASE_JPQL = "SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))";
    private static final String COUNT_JPQL = "SELECT COUNT(p) FROM Product p WHERE p.isActive = true AND " +
            "(:category IS NULL OR p.category = :category) AND " +
            "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            " LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))";

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "name", "description", "currentPrice", "originalPrice", "category",
            "rating", "reviewCount", "isBestSeller", "isActive", "createdAt", "updatedAt");

    private final EntityManager entityManager;

    public ProductRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Page<Product> findActiveWithFilters(ProductCategory category, String searchTerm, Pageable pageable) {
        String orderClause = toOrderClause(pageable.getSort());
        String dataJpql = BASE_JPQL + orderClause;

        TypedQuery<Product> dataQuery = entityManager.createQuery(dataJpql, Product.class);
        bindParameters(dataQuery, category, searchTerm);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        List<Product> content = dataQuery.getResultList();

        TypedQuery<Long> countQuery = entityManager.createQuery(COUNT_JPQL, Long.class);
        bindParameters(countQuery, category, searchTerm);
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private void bindParameters(TypedQuery<?> query, ProductCategory category, String searchTerm) {
        query.setParameter("category", category);
        query.setParameter("searchTerm", new TypedParameterValue<>(StandardBasicTypes.STRING, searchTerm));
    }

    private static String toOrderClause(Sort sort) {
        if (sort == null || sort.isEmpty()) {
            return " ORDER BY p.createdAt DESC";
        }
        StringBuilder sb = new StringBuilder(" ORDER BY ");
        boolean first = true;
        for (Sort.Order order : sort) {
            String property = order.getProperty();
            if (!ALLOWED_SORT_PROPERTIES.contains(property)) {
                continue;
            }
            if (!first) sb.append(", ");
            sb.append("p.").append(property).append(" ").append(order.getDirection().name());
            first = false;
        }
        if (first) {
            return " ORDER BY p.createdAt DESC";
        }
        return sb.toString();
    }
}
