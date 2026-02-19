package com.seaandtea.repository;

import com.seaandtea.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id = :productId ORDER BY pi.isPrimary DESC, pi.sortOrder ASC, pi.createdAt ASC")
    List<ProductImage> findByProductIdOrderByPrimaryAndSortOrder(@Param("productId") Long productId);

    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.id IN :productIds ORDER BY pi.product.id, pi.isPrimary DESC, pi.sortOrder ASC")
    List<ProductImage> findByProductIdIn(@Param("productIds") Collection<Long> productIds);
}
