package com.example.backend.service;

import com.example.backend.dto.BookingResponse;
import com.example.backend.dto.CreateBookingRequest;
import com.example.backend.enums.BookingStatus;
import com.example.backend.model.Booking;
import com.example.backend.model.User;
import com.example.backend.model.Venue;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VenueRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    public BookingResponse createBooking(String email, CreateBookingRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (request.getVenueId() == null) {
            throw new IllegalArgumentException("Venue id is required");
        }
        if (request.getBookingDate() == null) {
            throw new IllegalArgumentException("Booking date is required");
        }
        if (request.getSlot() == null || request.getSlot().isBlank()) {
            throw new IllegalArgumentException("Slot is required");
        }

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));

        if (Boolean.TRUE.equals(venue.getDisabled())) {
            throw new IllegalArgumentException("This venue is currently disabled");
        }

        LocalTime[] parsedSlot = parseSlot(request.getSlot());
        LocalTime startTime = parsedSlot[0];
        LocalTime endTime = parsedSlot[1];

        ensureNoSlotConflict(venue.getId(), request.getBookingDate(), startTime, endTime);

        Booking booking = Booking.builder()
            .user(user)
            .venue(venue)
            .bookingDate(request.getBookingDate())
            .startTime(startTime)
            .endTime(endTime)
            .status(BookingStatus.PENDING)
            .build();

        Booking saved = bookingRepository.save(booking);
        return toResponse(saved);
    }

    public List<BookingResponse> listBookings(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return bookingRepository.findUpcomingBookings(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void cancelBooking(String email, UUID bookingId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Booking booking = bookingRepository.findByIdAndUser_Id(bookingId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }

    private void ensureNoSlotConflict(UUID venueId, LocalDate bookingDate, LocalTime startTime, LocalTime endTime) {
        List<BookingStatus> activeStatuses = List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED);
        List<Booking> bookings = bookingRepository.findByVenue_IdAndBookingDateAndStatusIn(
                venueId,
                bookingDate,
                activeStatuses);

        boolean hasConflict = bookings.stream().anyMatch(existing ->
                startTime.isBefore(existing.getEndTime()) && existing.getStartTime().isBefore(endTime));

        if (hasConflict) {
            throw new IllegalArgumentException("Selected slot is already booked");
        }
    }

    private LocalTime[] parseSlot(String slot) {
        String[] parts = slot.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid slot format. Expected H:mm-H:mm");
        }

        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(parts[0].trim(), SLOT_FORMATTER);
            end = LocalTime.parse(parts[1].trim(), SLOT_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid slot format. Expected H:mm-H:mm");
        }

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Slot end time must be after start time");
        }

        return new LocalTime[] { start, end };
    }

    private BookingResponse toResponse(Booking booking) {
        Venue venue = booking.getVenue();

        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                venue.getId(),
                venue.getCourtName(),
                venue.getCourtLocation(),
                venue.getCourtImage(),
                venue.getSport(),
                booking.getBookingDate(),
                booking.getStartTime().format(SLOT_FORMATTER) + "-" + booking.getEndTime().format(SLOT_FORMATTER),
                booking.getStatus(),
                booking.getCreatedAt());
    }
}
