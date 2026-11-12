package com.seaandtea.controller;

import com.seaandtea.dto.TourListResponse;
import com.seaandtea.dto.WishlistAddRequest;
import com.seaandtea.service.WishlistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    public ResponseEntity<List<TourListResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(wishlistService.list(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Void> add(@Valid @RequestBody WishlistAddRequest request, Authentication authentication) {
        wishlistService.add(request.getTourId(), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{tourId}")
    public ResponseEntity<Void> remove(@PathVariable Long tourId, Authentication authentication) {
        wishlistService.remove(tourId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
