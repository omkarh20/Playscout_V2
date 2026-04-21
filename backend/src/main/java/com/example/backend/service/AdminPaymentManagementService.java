package com.example.backend.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.backend.dto.AdminPaymentResponse;
import com.example.backend.dto.AdminRefundRequestResponse;
import com.example.backend.enums.RefundStatus;
import com.example.backend.model.Booking;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PaymentRecordRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminPaymentManagementService {

    private final BookingRepository bookingRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final PaymentService paymentService;

    public long getTotalBookings() {
        return bookingRepository.count();
    }

    public List<AdminPaymentResponse> getAllPayments() {
        return paymentRecordRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(record -> {
                AdminPaymentResponse response = new AdminPaymentResponse();
                response.setId(record.getId());
                response.setOrderId(record.getOrderId());
                response.setUserId(record.getUserId());
                response.setCourtName(record.getCourtName());
                response.setCourtLocation(record.getCourtLocation());
                response.setSport(record.getSport());
                response.setBookingDate(record.getBookingDate());
                response.setBookingSlot(record.getBookingSlot());
                response.setAmount(record.getAmount());
                response.setCurrency(record.getCurrency());
                response.setStatus(record.getStatus());
                response.setCreatedAt(record.getCreatedAt());
                userRepository.findById(record.getUserId())
                    .ifPresent(user -> response.setUserEmail(user.getEmail()));
                return response;
            })
            .collect(Collectors.toList());
    }

    public List<AdminRefundRequestResponse> getRefundRequests() {
        return bookingRepository.findByRefundStatusOrderByRefundRequestedAtDesc(RefundStatus.REQUESTED)
            .stream()
            .map(this::toRefundResponse)
            .collect(Collectors.toList());
    }

    public Booking processRefund(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getRefundStatus() != RefundStatus.REQUESTED) {
            throw new IllegalArgumentException("Refund is not requested for this booking");
        }

        if (booking.getPaymentIntentId() == null || booking.getPaymentIntentId().isBlank()) {
            throw new IllegalArgumentException("Missing payment intent for refund");
        }

        try {
            var refund = paymentService.refundPaymentIntent(booking.getPaymentIntentId());
            booking.setRefundStatus(RefundStatus.REFUNDED);
            booking.setRefundId(refund.getId());
            booking.setRefundProcessedAt(OffsetDateTime.now());
            return bookingRepository.save(booking);
        } catch (com.stripe.exception.StripeException ex) {
            booking.setRefundStatus(RefundStatus.FAILED);
            booking.setRefundProcessedAt(OffsetDateTime.now());
            bookingRepository.save(booking);
            throw new IllegalArgumentException("Refund failed: " + ex.getMessage());
        }
    }

    private AdminRefundRequestResponse toRefundResponse(Booking booking) {
        AdminRefundRequestResponse response = new AdminRefundRequestResponse();
        response.setBookingId(booking.getId());
        response.setUserId(booking.getUserId());
        response.setBookingDate(booking.getBookingDate());
        response.setBookingSlot(booking.getBookingSlot());
        response.setRefundStatus(booking.getRefundStatus());
        response.setRefundRequestedAt(booking.getRefundRequestedAt());
        venueRepository.findById(booking.getVenueId()).ifPresent(venue -> {
            response.setCourtName(venue.getCourtName());
            response.setCourtLocation(venue.getCourtLocation());
            response.setSport(venue.getSport());
            response.setPrice(venue.getPrice());
        });
        userRepository.findById(booking.getUserId())
            .ifPresent(user -> response.setUserEmail(user.getEmail()));
        return response;
    }
}
