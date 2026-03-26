package com.example.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.backend.enums.BookingStatus;
import com.example.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {
    List<Booking> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    List<Booking> findByVenue_IdAndBookingDateAndStatusIn(
        UUID venueId,
        LocalDate bookingDate,
        List<BookingStatus> statuses
    );

    Optional<Booking> findByIdAndUser_Id(UUID bookingId, UUID userId);
}
