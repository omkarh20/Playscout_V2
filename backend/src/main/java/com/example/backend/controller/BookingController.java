package com.example.backend.controller;

import com.example.backend.dto.BookingRequest;
import com.example.backend.model.Booking;
import com.example.backend.service.BookingService;
import com.example.backend.template.BookingProcessor;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final BookingProcessor bookingProcessor;

    public BookingController(BookingService bookingService, BookingProcessor bookingProcessor) {
        this.bookingService = bookingService;
        this.bookingProcessor = bookingProcessor;
    }

    @PostMapping("/add-booking")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Map<String, Object>> addBooking(
        Authentication authentication,
        @Valid @RequestBody BookingRequest request
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "User is not logged in"
            ));
        }
        String actor = authentication.getName();
        Booking booking = bookingProcessor.processAction(actor, request);

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Booking saved successfully",
            "booking", booking
        ));
    }

    @GetMapping({ "", "/list-bookings" })
    public ResponseEntity<Map<String, Object>> listBookings(Authentication authentication) {
        String userId = authentication != null ? authentication.getName() : "";
        return ResponseEntity.ok(Map.of("success", true, "data", bookingService.listByUser(userId)));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Map<String, Object>> updateBookingStatus(
        Authentication authentication,
        @RequestBody Map<String, String> body,
        @PathVariable UUID id
    ) {
        String status = body.get("status");
        if (status == null || !"CANCELLED".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Only cancellation is supported"));
        }
        return cancelBooking(authentication, Map.of("id", id.toString()));
    }

    @PostMapping("/cancel-booking")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Map<String, Object>> cancelBooking(
        Authentication authentication,
        @RequestBody Map<String, String> body
    ) {
        String userId = authentication != null ? authentication.getName() : "";
        if (!body.containsKey("id")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Missing booking id"));
        }
        UUID bookingId;
        try {
            bookingId = UUID.fromString(body.get("id"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Invalid booking id"));
        }
        var cancelledOpt = bookingService.cancelBooking(bookingId, userId);
        if (cancelledOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Could not cancel booking"));
        }
        Booking booking = cancelledOpt.get();
        if (booking.getRefundStatus() != null && booking.getRefundStatus().name().equals("REQUESTED")) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Booking cancelled. Refund requested."));
        }
        return ResponseEntity.ok(Map.of("success", true, "message", "Booking cancelled"));
    }
}
