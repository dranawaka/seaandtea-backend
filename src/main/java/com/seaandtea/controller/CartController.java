package com.seaandtea.controller;

import com.seaandtea.dto.*;
import com.seaandtea.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
@Tag(name = "Shopping Cart", description = "Add products to cart, update quantity, remove items")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get my cart", description = "Get the current user's cart with all items", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cart retrieved", content = @Content(schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        CartResponse response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/items")
    @Operation(summary = "Add to cart", description = "Add a product to the cart or increase quantity", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item added", content = @Content(schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or product unavailable"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ResponseEntity<CartResponse> addToCart(
            @Valid @RequestBody AddToCartRequest request,
            Authentication authentication) {
        CartResponse response = cartService.addItem(authentication.getName(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity", description = "Update the quantity of an item in the cart", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Quantity updated", content = @Content(schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid quantity"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartResponse> updateCartItem(
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request,
            Authentication authentication) {
        CartResponse response = cartService.updateItemQuantity(authentication.getName(), itemId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart", description = "Remove an item from the cart", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Item removed", content = @Content(schema = @Schema(implementation = CartResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Cart item not found")
    })
    public ResponseEntity<CartResponse> removeCartItem(
            @PathVariable Long itemId,
            Authentication authentication) {
        CartResponse response = cartService.removeItem(authentication.getName(), itemId);
        return ResponseEntity.ok(response);
    }
}
