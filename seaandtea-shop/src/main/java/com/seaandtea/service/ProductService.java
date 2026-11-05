package com.seaandtea.service;

import com.seaandtea.dto.*;
import com.seaandtea.entity.Product;
import com.seaandtea.entity.ProductImage;
import com.seaandtea.entity.Product.ProductCategory;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.ProductImageRepository;
import com.seaandtea.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final FileUploadService fileUploadService;

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());
        if (request.getCategory() == ProductCategory.ALL) {
            throw new IllegalArgumentException("Category 'all' is only for filtering; use sea, tea, spices, clothing, souvenirs, beauty, or other");
        }
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .currentPrice(request.getCurrentPrice())
                .originalPrice(request.getOriginalPrice())
                .category(request.getCategory())
                .rating(request.getRating())
                .reviewCount(request.getReviewCount() != null ? request.getReviewCount() : 0)
                .isBestSeller(request.getIsBestSeller() != null ? request.getIsBestSeller() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
        product = productRepository.save(product);
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            addImagesToProduct(product, request.getImageUrls(), request.getPrimaryImageIndex() != null ? request.getPrimaryImageIndex() : 0);
        }
        log.info("Product created with ID: {}", product.getId());
        return toProductResponse(loadProductWithImages(product.getId()));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        log.info("Updating product: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getCurrentPrice() != null) product.setCurrentPrice(request.getCurrentPrice());
        if (request.getOriginalPrice() != null) product.setOriginalPrice(request.getOriginalPrice());
        if (request.getCategory() != null) {
            if (request.getCategory() == ProductCategory.ALL) {
                throw new IllegalArgumentException("Category 'all' is only for filtering; use sea, tea, spices, clothing, souvenirs, beauty, or other");
            }
            product.setCategory(request.getCategory());
        }
        if (request.getRating() != null) product.setRating(request.getRating());
        if (request.getReviewCount() != null) product.setReviewCount(request.getReviewCount());
        if (request.getIsBestSeller() != null) product.setIsBestSeller(request.getIsBestSeller());
        if (request.getIsActive() != null) product.setIsActive(request.getIsActive());
        if (request.getImageUrls() != null) {
            productImageRepository.deleteByProductId(id);
            if (!request.getImageUrls().isEmpty()) {
                int primaryIdx = request.getPrimaryImageIndex() != null ? request.getPrimaryImageIndex() : 0;
                addImagesToProduct(product, request.getImageUrls(), primaryIdx);
            }
        }
        product = productRepository.save(product);
        return toProductResponse(loadProductWithImages(product.getId()));
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product: {}", id);
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = loadProductWithImages(id);
        if (product == null) throw new ResourceNotFoundException("Product", id);
        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getActiveById(Long id) {
        Product product = loadProductWithImages(id);
        if (product == null || !Boolean.TRUE.equals(product.getIsActive())) {
            throw new ResourceNotFoundException("Product", id);
        }
        return toProductResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getActiveProducts(ProductCategory category, String searchTerm,
                                                       int page, int size, String sortBy, String sortDirection) {
        // "all" means no category filter
        ProductCategory filterCategory = (category == null || category == ProductCategory.ALL) ? null : category;
        Sort sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findActiveWithFilters(filterCategory, searchTerm, pageable);
        Map<Long, List<String>> imagesByProductId = fetchImageUrlsByProductIds(
                products.getContent().stream().map(Product::getId).toList());
        return products.map(p -> toProductListResponse(p, imagesByProductId.getOrDefault(p.getId(), Collections.emptyList())));
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getBestSellers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findActiveBestSellers(pageable);
        Map<Long, List<String>> imagesByProductId = fetchImageUrlsByProductIds(
                products.getContent().stream().map(Product::getId).toList());
        return products.map(p -> toProductListResponse(p, imagesByProductId.getOrDefault(p.getId(), Collections.emptyList())));
    }

    @Transactional(readOnly = true)
    public Page<ProductListResponse> getByCategory(ProductCategory category, int page, int size) {
        if (category == null || category == ProductCategory.ALL) {
            return getActiveProducts(null, null, page, size, "createdAt", "desc");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findActiveByCategory(category, pageable);
        Map<Long, List<String>> imagesByProductId = fetchImageUrlsByProductIds(
                products.getContent().stream().map(Product::getId).toList());
        return products.map(p -> toProductListResponse(p, imagesByProductId.getOrDefault(p.getId(), Collections.emptyList())));
    }

    /** Add a single image to a product (e.g. after upload). Admin only. */
    @Transactional
    public ProductResponse addImageToProduct(Long productId, String imageUrl, Boolean isPrimary, String altText) {
        Product product = productRepository.findByIdWithImages(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        if (product.getImages() != null && product.getImages().size() >= 10) {
            throw new IllegalArgumentException("Product already has maximum 10 images");
        }
        if (Boolean.TRUE.equals(isPrimary)) {
            productImageRepository.clearPrimaryForProduct(productId);
        }
        int nextOrder = product.getImages() == null || product.getImages().isEmpty()
                ? 0
                : product.getImages().stream().mapToInt(ProductImage::getSortOrder).max().orElse(0) + 1;
        ProductImage img = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .isPrimary(Boolean.TRUE.equals(isPrimary))
                .sortOrder(nextOrder)
                .altText(altText)
                .build();
        productImageRepository.save(img);
        return toProductResponse(loadProductWithImages(productId));
    }

    /** Remove a single image from a product. Deletes from DB and from storage. Admin only. */
    @Transactional
    public ProductResponse removeImageFromProduct(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        if (!productId.equals(image.getProduct().getId())) {
            throw new ResourceNotFoundException("ProductImage", imageId);
        }
        String imageUrl = image.getImageUrl();
        productImageRepository.delete(image);
        try {
            fileUploadService.deleteImage(imageUrl);
        } catch (Exception e) {
            log.warn("Could not delete image from storage: {}", imageUrl, e);
        }
        return toProductResponse(loadProductWithImages(productId));
    }

    /** Update a product image (primary flag, sort order, alt text). Admin only. */
    @Transactional
    public ProductResponse updateProductImage(Long productId, Long imageId, ProductImageUpdateRequest request) {
        if (request == null) return getById(productId);
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        if (!productId.equals(image.getProduct().getId())) {
            throw new ResourceNotFoundException("ProductImage", imageId);
        }
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            productImageRepository.clearPrimaryForProduct(productId);
            image.setIsPrimary(true);
        }
        if (request.getSortOrder() != null) image.setSortOrder(request.getSortOrder());
        if (request.getAltText() != null) image.setAltText(request.getAltText());
        productImageRepository.save(image);
        return toProductResponse(loadProductWithImages(productId));
    }

    private Product loadProductWithImages(Long productId) {
        return productRepository.findByIdWithImages(productId).orElse(null);
    }

    private void addImagesToProduct(Product product, List<String> imageUrls, int primaryImageIndex) {
        int primary = (primaryImageIndex >= 0 && primaryImageIndex < imageUrls.size()) ? primaryImageIndex : 0;
        for (int i = 0; i < imageUrls.size(); i++) {
            String url = imageUrls.get(i);
            if (url == null || url.isBlank()) continue;
            ProductImage img = ProductImage.builder()
                    .product(product)
                    .imageUrl(url.trim())
                    .isPrimary(i == primary)
                    .sortOrder(i)
                    .build();
            productImageRepository.save(img);
        }
    }

    private ProductResponse toProductResponse(Product p) {
        List<ProductResponse.ProductImageDto> imageDtos = new ArrayList<>();
        if (p.getImages() != null) {
            for (ProductImage pi : p.getImages()) {
                imageDtos.add(ProductResponse.ProductImageDto.builder()
                        .id(pi.getId())
                        .imageUrl(pi.getImageUrl())
                        .isPrimary(Boolean.TRUE.equals(pi.getIsPrimary()))
                        .altText(pi.getAltText())
                        .sortOrder(pi.getSortOrder())
                        .createdAt(pi.getCreatedAt())
                        .build());
            }
        }
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .images(imageDtos)
                .currentPrice(p.getCurrentPrice())
                .originalPrice(p.getOriginalPrice())
                .discountPercentage(p.getDiscountPercentage())
                .category(p.getCategory())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .isBestSeller(Boolean.TRUE.equals(p.getIsBestSeller()))
                .isActive(Boolean.TRUE.equals(p.getIsActive()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private Map<Long, List<String>> fetchImageUrlsByProductIds(List<Long> productIds) {
        if (productIds.isEmpty()) return Map.of();
        return productImageRepository.findByProductIdIn(productIds).stream()
                .collect(Collectors.groupingBy(pi -> pi.getProduct().getId(),
                        Collectors.mapping(ProductImage::getImageUrl, Collectors.toList())));
    }

    private ProductListResponse toProductListResponse(Product p, List<String> imageUrls) {
        return ProductListResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .imageUrls(imageUrls)
                .currentPrice(p.getCurrentPrice())
                .originalPrice(p.getOriginalPrice())
                .discountPercentage(p.getDiscountPercentage())
                .category(p.getCategory())
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .isBestSeller(Boolean.TRUE.equals(p.getIsBestSeller()))
                .build();
    }
}
