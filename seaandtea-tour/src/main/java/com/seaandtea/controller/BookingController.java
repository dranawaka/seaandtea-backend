package com.seaandtea.controller;

import com.seaandtea.dto.BookingCreateRequest;
import com.seaandtea.dto.BookingResponse;
import com.seaandtea.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('GUIDE') or hasRole('ADMIN')")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(
            @Valid @RequestBody BookingCreateRequest request,
            Authentication authentication) {
        BookingResponse response = bookingService.createBooking(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(bookingService.listMyBookings(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> get(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.getMyBooking(id, authentication.getName()));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancel(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(bookingService.cancelMyBooking(id, authentication.getName()));
    }
}
