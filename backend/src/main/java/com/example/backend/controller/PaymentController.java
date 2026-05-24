package com.example.backend.controller;

import com.example.backend.dto.BookingRequest;
import com.example.backend.dto.PaymentCheckoutRequest;
import com.example.backend.dto.PaymentCheckoutResponse;
import com.example.backend.dto.PaymentHistoryResponse;
import com.example.backend.enums.BookingStatus;
import com.example.backend.model.Booking;
import com.example.backend.service.BookingService;
import com.example.backend.service.PaymentService;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.UserRepository;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public PaymentController(
        PaymentService paymentService,
        BookingService bookingService,
        BookingRepository bookingRepository,
        UserRepository userRepository
    ) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/checkout-session")
    @PreAuthorize("hasRole('PLAYER')")
    public PaymentCheckoutResponse createCheckoutSession(@Valid @RequestBody PaymentCheckoutRequest request)
        throws StripeException {
        return paymentService.createCheckoutSession(request);
    }

    @GetMapping("/checkout-session/{sessionId}")
    public ResponseEntity<?> getCheckoutSession(@PathVariable String sessionId) throws StripeException {
        var session = paymentService.getCheckoutSession(sessionId);
        String paymentStatus = session.getPaymentStatus();
        if (paymentStatus != null && isPaidStatus(paymentStatus)) {
            Map<String, String> metadata = session.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                BookingRequest bookingRequest = new BookingRequest();
                bookingRequest.setUserId(metadata.get("userId"));
                bookingRequest.setVenueId(metadata.get("venueId"));
                bookingRequest.setBookingDate(metadata.get("bookingDate"));
                bookingRequest.setBookingSlot(metadata.get("bookingSlot"));
                bookingRequest.setOrderId(metadata.get("orderId"));
                bookingRequest.setPaymentIntentId(session.getPaymentIntent());
                bookingService.confirmOrCreateBooking(bookingRequest);
                if (bookingRequest.getOrderId() != null && !bookingRequest.getOrderId().isBlank()) {
                    paymentService.markPaid(bookingRequest.getOrderId(), session.getPaymentIntent());
                }
            }
        }
        return ResponseEntity.ok(Map.of(
            "success", true,
            "status", session.getPaymentStatus(),
            "sessionId", session.getId(),
            "amountTotal", session.getAmountTotal(),
            "currency", session.getCurrency()
        ));
    }

    @PostMapping("/checkout-session/booking/{bookingId}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> createCheckoutSessionForBooking(
        @PathVariable UUID bookingId,
        Authentication authentication
    ) throws StripeException {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Unauthorized"
            ));
        }

        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        if (booking == null) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Booking not found"
            ));
        }

        UUID userId = resolveUserId(authentication.getName());
        if (userId == null || !userId.equals(booking.getUserId())) {
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "Not allowed"
            ));
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Booking is not pending"
            ));
        }

        if (!isBeforeSlot(booking)) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Payment window has closed for this booking"
            ));
        }

        var response = paymentService.createCheckoutSessionForBooking(booking);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<?> getPaymentHistory(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(Map.of(
                "success", false,
                "message", "Unauthorized"
            ));
        }
        String userId = authentication.getName();
        var payments = paymentService.listPaymentsForUser(userId)
            .stream()
            .map(record -> {
                PaymentHistoryResponse resp = new PaymentHistoryResponse();
                resp.setId(record.getId());
                resp.setOrderId(record.getOrderId());
                resp.setCourtName(record.getCourtName());
                resp.setCourtLocation(record.getCourtLocation());
                resp.setSport(record.getSport());
                resp.setBookingDate(record.getBookingDate());
                resp.setBookingSlot(record.getBookingSlot());
                resp.setAmount(record.getAmount());
                resp.setCurrency(record.getCurrency());
                resp.setStatus(record.getStatus());
                resp.setCreatedAt(record.getCreatedAt());
                return resp;
            })
            .toList();

        return ResponseEntity.ok(Map.of(
            "success", true,
            "data", payments
        ));
    }

    private boolean isPaidStatus(String status) {
        String normalized = status.toLowerCase(Locale.ROOT);
        return normalized.equals("paid") || normalized.equals("succeeded") || normalized.equals("complete");
    }

    private UUID resolveUserId(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            return userRepository.findByEmail(rawValue)
                .map(user -> user.getId())
                .orElse(null);
        }
    }

    private boolean isBeforeSlot(Booking booking) {
        LocalDate date = parseBookingDate(booking.getBookingDate());
        LocalTime startTime = booking.getStartTime();
        if (date == null || startTime == null) {
            return true;
        }
        return java.time.LocalDateTime.now().isBefore(date.atTime(startTime));
    }

    private LocalDate parseBookingDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ISO_LOCAL_DATE
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(raw.trim(), formatter);
            } catch (DateTimeParseException ex) {
                // try next
            }
        }
        return null;
    }
}
