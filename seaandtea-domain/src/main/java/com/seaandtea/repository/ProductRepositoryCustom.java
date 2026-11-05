package com.seaandtea.repository;

import com.seaandtea.entity.Product;
import com.seaandtea.entity.Product.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {

    Page<Product> findActiveWithFilters(ProductCategory category, String searchTerm, Pageable pageable);
}
