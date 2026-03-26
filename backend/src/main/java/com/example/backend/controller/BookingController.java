package com.example.backend.controller;

import com.example.backend.dto.CancelBookingRequest;
import com.example.backend.dto.CreateBookingRequest;
import com.example.backend.dto.BookingResponse;
import com.example.backend.service.BookingService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/add-booking")
    public ResponseEntity<Map<String, Object>> addBooking(
            Authentication authentication,
            @RequestBody CreateBookingRequest request) {
        try {
            BookingResponse booking = bookingService.createBooking(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Booking created successfully", "data", booking));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/list-bookings")
    public ResponseEntity<Map<String, Object>> listBookings(Authentication authentication) {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", bookingService.listBookings(authentication.getName())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping("/cancel-booking")
    public ResponseEntity<Map<String, Object>> cancelBooking(
            Authentication authentication,
            @RequestBody CancelBookingRequest request) {
        try {
            bookingService.cancelBooking(authentication.getName(), request.getId());
            return ResponseEntity.ok(Map.of("success", true, "message", "Booking cancelled successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
