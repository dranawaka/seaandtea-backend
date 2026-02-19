package com.seaandtea.repository;

import com.seaandtea.entity.Product;
import com.seaandtea.entity.Product.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Product> findByIdWithImages(@Param("id") Long id);

    Page<Product> findByIsActiveTrue(Pageable pageable);

    Page<Product> findActiveWithFilters(
            @Param("category") ProductCategory category,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isBestSeller = true")
    Page<Product> findActiveBestSellers(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.category = :category")
    Page<Product> findActiveByCategory(@Param("category") ProductCategory category, Pageable pageable);
}
