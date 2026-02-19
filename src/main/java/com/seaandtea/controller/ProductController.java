package com.seaandtea.controller;

import com.seaandtea.dto.*;
import com.seaandtea.entity.Product.ProductCategory;
import com.seaandtea.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Product Management", description = "Product catalog and admin product CRUD")
public class ProductController {

    private final ProductService productService;

    // ---------- Public endpoints (browse products, add to cart from frontend) ----------

    @GetMapping
    @Operation(summary = "List products", description = "Get paginated list of active products with optional filters")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    })
    public ResponseEntity<Page<ProductListResponse>> getProducts(
            @Parameter(description = "Filter by category") @RequestParam(required = false) ProductCategory category,
            @Parameter(description = "Search in name and description") @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        Page<ProductListResponse> response = productService.getActiveProducts(
                category, (searchTerm != null && searchTerm.isBlank()) ? null : searchTerm, page, size, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Get a single active product by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product found", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getActiveById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/best-sellers")
    @Operation(summary = "Get best sellers", description = "Get products marked as best sellers")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Best sellers retrieved") })
    public ResponseEntity<Page<ProductListResponse>> getBestSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getBestSellers(page, size));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get products by category", description = "Get active products in a category")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Products retrieved") })
    public ResponseEntity<Page<ProductListResponse>> getByCategory(
            @PathVariable ProductCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getByCategory(category, page, size));
    }

    // ---------- Admin endpoints ----------

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create product (Admin)", description = "Create a new product", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only")
    })
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product (Admin)", description = "Update an existing product", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request) {
        ProductResponse response = productService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product (Admin)", description = "Permanently delete a product", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove product image (Admin)", description = "Remove one image from a product; also deletes from storage", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image removed", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Product or image not found")
    })
    public ResponseEntity<ProductResponse> removeProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId) {
        ProductResponse response = productService.removeImageFromProduct(productId, imageId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product image (Admin)", description = "Set primary, sort order, or alt text for an image", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image updated", content = @Content(schema = @Schema(implementation = ProductResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin only"),
        @ApiResponse(responseCode = "404", description = "Product or image not found")
    })
    public ResponseEntity<ProductResponse> updateProductImage(
            @PathVariable Long productId,
            @PathVariable Long imageId,
            @RequestBody(required = false) ProductImageUpdateRequest request) {
        ProductResponse response = productService.updateProductImage(productId, imageId, request);
        return ResponseEntity.ok(response);
    }
}
