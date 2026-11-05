package com.seaandtea.service;

import com.seaandtea.dto.*;
import com.seaandtea.entity.Cart;
import com.seaandtea.entity.CartItem;
import com.seaandtea.entity.Product;
import com.seaandtea.entity.ProductImage;
import com.seaandtea.exception.ResourceNotFoundException;
import com.seaandtea.repository.CartItemRepository;
import com.seaandtea.repository.CartRepository;
import com.seaandtea.repository.ProductImageRepository;
import com.seaandtea.repository.ProductRepository;
import com.seaandtea.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getOrCreateCart(String userEmail) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().user(user).build();
                    return cartRepository.save(newCart);
                });
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String userEmail, AddToCartRequest request) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", request.getProductId()));
        if (!Boolean.TRUE.equals(product.getIsActive())) {
            throw new IllegalArgumentException("Product is not available for purchase");
        }
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));

        var existing = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cartItemRepository.save(item);
            cart.getItems().add(item);
        }
        return toCartResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(String userEmail, Long cartItemId, UpdateCartItemRequest request) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));
        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return toCartResponse(item.getCart());
    }

    @Transactional
    public CartResponse removeItem(String userEmail, Long cartItemId) {
        var user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        CartItem item = cartItemRepository.findByIdAndUserId(cartItemId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart item", cartItemId));
        Cart cart = item.getCart();
        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return toCartResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        return getOrCreateCart(userEmail);
    }

    private CartResponse toCartResponse(Cart cart) {
        List<CartItem> itemsWithProduct = cartItemRepository.findByCartIdWithProduct(cart.getId());
        List<CartItemResponse> itemResponses = itemsWithProduct.stream()
                .map(this::toCartItemResponse)
                .collect(Collectors.toList());
        BigDecimal total = itemResponses.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int count = itemResponses.stream()
                .mapToInt(CartItemResponse::getQuantity)
                .sum();
        return CartResponse.builder()
                .cartId(cart.getId())
                .items(itemResponses)
                .itemCount(count)
                .totalAmount(total)
                .build();
    }

    private CartItemResponse toCartItemResponse(CartItem item) {
        Product p = item.getProduct();
        BigDecimal unitPrice = p.getCurrentPrice();
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
        String imageUrl = productImageRepository.findByProductIdOrderByPrimaryAndSortOrder(p.getId()).stream()
                .findFirst()
                .map(ProductImage::getImageUrl)
                .orElse(null);
        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .imageUrl(imageUrl)
                .unitPrice(unitPrice)
                .quantity(item.getQuantity())
                .lineTotal(lineTotal)
                .build();
    }
}
