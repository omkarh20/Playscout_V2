package com.example.backend.service;

import com.example.backend.dto.BookingRequest;
import com.example.backend.dto.BookingResponse;
import com.example.backend.enums.BookingStatus;
import com.example.backend.enums.RefundStatus;
import com.example.backend.model.Booking;
import com.example.backend.model.Venue;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VenueRepository;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    public BookingService(
        BookingRepository bookingRepository,
        UserRepository userRepository,
        VenueRepository venueRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
    }

    @Transactional
    public Booking createBooking(BookingRequest request) {
        Booking booking = buildBooking(request);
        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking createPendingBooking(BookingRequest request) {
        Booking booking = buildBooking(request);
        booking.setStatus(BookingStatus.PENDING);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking confirmOrCreateBooking(BookingRequest request) {
        String orderId = request.getOrderId();
        if (orderId != null && !orderId.isBlank()) {
            Optional<Booking> existingByOrder = bookingRepository.findFirstByOrderId(orderId);
            if (existingByOrder.isPresent()) {
                Booking existing = existingByOrder.get();
                existing.setStatus(BookingStatus.CONFIRMED);
                if (request.getPaymentIntentId() != null && !request.getPaymentIntentId().isBlank()) {
                    existing.setPaymentIntentId(request.getPaymentIntentId());
                }
                return bookingRepository.save(existing);
            }
        }

        UUID userId = resolveUserId(request.getUserId());
        UUID venueId = resolveVenueId(request.getVenueId());
        if (userId == null || venueId == null) {
            throw new IllegalArgumentException("Invalid user or venue id");
        }

        return bookingRepository
            .findFirstByUserIdAndVenueIdAndBookingDateAndBookingSlotAndStatus(
                userId,
                venueId,
                request.getBookingDate(),
                request.getBookingSlot(),
                BookingStatus.PENDING
            )
            .map(existing -> {
                existing.setStatus(BookingStatus.CONFIRMED);
                if (request.getPaymentIntentId() != null && !request.getPaymentIntentId().isBlank()) {
                    existing.setPaymentIntentId(request.getPaymentIntentId());
                }
                return bookingRepository.save(existing);
            })
            .orElseGet(() -> {
                Booking booking = buildBooking(request);
                booking.setStatus(BookingStatus.CONFIRMED);
                return bookingRepository.save(booking);
            });
    }

    public List<BookingResponse> listByUser(String userId) {
        UUID resolvedUserId = resolveUserId(userId);
        if (resolvedUserId == null) {
            return List.of();
        }
        return bookingRepository.findByUserIdAndStatusNot(resolvedUserId, BookingStatus.CANCELLED)
            .stream()
            .map(this::toBookingResponse)
            .toList();
    }

    @Transactional
    public Optional<Booking> cancelBooking(UUID bookingId, String userId) {
        UUID resolvedUserId = resolveUserId(userId);
        if (resolvedUserId == null) {
            return Optional.empty();
        }
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return Optional.empty();
        }

        Booking booking = bookingOpt.get();
        if (!booking.getUserId().equals(resolvedUserId)) {
            return Optional.empty();
        }

        booking.setStatus(BookingStatus.CANCELLED);
        if (booking.getPaymentIntentId() != null && !booking.getPaymentIntentId().isBlank()) {
            booking.setRefundStatus(RefundStatus.REQUESTED);
            booking.setRefundRequestedAt(OffsetDateTime.now());
        } else {
            booking.setRefundStatus(RefundStatus.NONE);
        }
        bookingRepository.save(booking);
        return Optional.of(booking);
    }

    private Booking buildBooking(BookingRequest request) {
        UUID userId = resolveUserId(request.getUserId());
        UUID venueId = resolveVenueId(request.getVenueId());
        if (userId == null || venueId == null) {
            throw new IllegalArgumentException("Invalid user or venue id");
        }
        Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new IllegalArgumentException("Venue not found"));
        Booking booking = new Booking();
        booking.setUserId(userId);
        booking.setVenueId(venue.getId());
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingSlot(request.getBookingSlot());
        LocalTime[] slotTimes = parseSlotTimes(request.getBookingSlot());
        booking.setStartTime(slotTimes[0]);
        booking.setEndTime(slotTimes[1]);
        booking.setOrderId(request.getOrderId());
        booking.setPaymentIntentId(request.getPaymentIntentId());
        return booking;
    }

    private BookingResponse toBookingResponse(Booking booking) {
        Venue venue = venueRepository.findById(booking.getVenueId()).orElse(null);
        return new BookingResponse(
            booking.getId(),
            booking.getUserId(),
            booking.getVenueId(),
            venue != null ? venue.getCourtName() : null,
            venue != null ? venue.getCourtLocation() : null,
            venue != null ? venue.getCourtImage() : null,
            venue != null ? venue.getSport() : null,
            booking.getBookingDate(),
            booking.getBookingSlot(),
            booking.getStatus(),
            booking.getRefundStatus(),
            booking.getCreatedAt()
        );
    }

    private LocalTime[] parseSlotTimes(String bookingSlot) {
        if (bookingSlot == null || bookingSlot.isBlank()) {
            throw new IllegalArgumentException("Booking slot is required");
        }
        String[] parts = bookingSlot.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid booking slot format: " + bookingSlot);
        }
        LocalTime start = parseTime(parts[0].trim(), bookingSlot);
        LocalTime end = parseTime(parts[1].trim(), bookingSlot);
        return new LocalTime[] { start, end };
    }

    private LocalTime parseTime(String raw, String bookingSlot) {
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
            DateTimeFormatter.ofPattern("H:mm"),
            DateTimeFormatter.ofPattern("HH:mm")
        };
        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalTime.parse(raw, formatter);
            } catch (DateTimeParseException ex) {
                // Try next pattern.
            }
        }
        throw new IllegalArgumentException("Invalid time in booking slot: " + bookingSlot);
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

    private UUID resolveVenueId(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
