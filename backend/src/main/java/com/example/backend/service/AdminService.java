package com.example.backend.service;

import com.example.backend.dto.AdminDashboardResponse;
import com.example.backend.dto.AdminGameResponse;
import com.example.backend.dto.AdminPaymentResponse;
import com.example.backend.dto.AdminRefundRequestResponse;
import com.example.backend.dto.AdminUserResponse;
import com.example.backend.dto.AdminVenueResponse;
import com.example.backend.model.Booking;
import com.example.backend.model.Game;
import com.example.backend.enums.RefundStatus;
import com.example.backend.model.User;
import com.example.backend.model.Venue;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.GameRepository;
import com.example.backend.repository.PaymentRecordRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VenueRepository venueRepository;
    private final GameRepository gameRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final PaymentService paymentService;

    public AdminDashboardResponse getDashboard() {
        long totalUsers = userRepository.count();
        long totalVenues = venueRepository.count();
        long totalGames = gameRepository.count();
        long totalBookings = bookingRepository.count();

        return new AdminDashboardResponse(totalUsers, totalVenues, totalGames, totalBookings);
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            AdminUserResponse response = new AdminUserResponse();
            response.setId(user.getId());
            response.setName(user.getName());
            response.setEmail(user.getEmail());
            response.setRole(user.getRole());
            response.setSuspended(Boolean.TRUE.equals(user.getSuspended()));
            return response;
        }).collect(Collectors.toList());
    }

    public List<AdminVenueResponse> getAllVenues() {
        return venueRepository.findAll().stream().map(venue -> {
            AdminVenueResponse response = new AdminVenueResponse();
            response.setId(venue.getId());
            response.setCourtName(venue.getCourtName());
            response.setSport(venue.getSport());
            response.setCourtLocation(venue.getCourtLocation());
            response.setCreatedBy(venue.getCreatedBy());
            response.setDisabled(Boolean.TRUE.equals(venue.getDisabled()));
            response.setPrice(venue.getPrice());

            // Fetch manager name
            if (venue.getCreatedBy() != null) {
                userRepository.findById(venue.getCreatedBy()).ifPresent(user -> 
                    response.setManagerName(user.getName())
                );
            }
            
            return response;
        }).collect(Collectors.toList());
    }

    public List<AdminGameResponse> getRecentGames(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"));
        return gameRepository.findAll(pageable).stream().map(game -> {
            AdminGameResponse response = new AdminGameResponse();
            response.setId(game.getId());
            response.setCourtName(game.getVenue().getCourtName());
            response.setSport(game.getVenue().getSport());
            response.setDate(game.getDate());
            response.setStartTime(game.getStartTime());
            response.setEndTime(game.getEndTime());
            response.setCreatorName(game.getCreatedBy().getName());
            response.setCreatedById(game.getCreatedBy().getId());
            response.setStatus(game.getStatus() != null ? game.getStatus() : "ACTIVE");
            response.setTotalMembers(game.getTotalMembers());
            response.setMembersJoined(game.getMembersJoined());
            return response;
        }).collect(Collectors.toList());
    }

    public void suspendUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setSuspended(!user.getSuspended());
        userRepository.save(user);
    }

    public void disableVenue(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("Venue not found"));
        venue.setDisabled(!venue.getDisabled());
        venueRepository.save(venue);
    }

    public void cancelGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if ("CANCELLED".equals(game.getStatus())) {
            throw new IllegalArgumentException("Game is already cancelled");
        }
        game.setStatus("CANCELLED");
        gameRepository.save(game);
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
            booking.setRefundProcessedAt(java.time.OffsetDateTime.now());
            return bookingRepository.save(booking);
        } catch (com.stripe.exception.StripeException ex) {
            booking.setRefundStatus(RefundStatus.FAILED);
            booking.setRefundProcessedAt(java.time.OffsetDateTime.now());
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
        Optional<User> userOpt = userRepository.findById(booking.getUserId());
        userOpt.ifPresent(user -> response.setUserEmail(user.getEmail()));
        return response;
    }
}
