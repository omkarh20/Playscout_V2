package com.example.backend.service;

import com.example.backend.dto.BookingRequest;
import com.example.backend.dto.PaymentCheckoutRequest;
import com.example.backend.dto.PaymentCheckoutResponse;
import com.example.backend.enums.PaymentStatus;
import com.example.backend.model.PaymentRecord;
import com.example.backend.model.Venue;
import com.example.backend.repository.BookingRepository;
import com.example.backend.repository.PaymentRecordRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VenueRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.checkout.Session;
import com.stripe.net.RequestOptions;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final BookingService bookingService;
    private final PaymentRecordRepository paymentRecordRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final VenueRepository venueRepository;

    @Value("${stripe.secretKey}")
    private String secretKey;

    @Value("${app.baseUrl}")
    private String baseUrl;

    public PaymentService(
        BookingService bookingService,
        PaymentRecordRepository paymentRecordRepository,
        BookingRepository bookingRepository,
        UserRepository userRepository,
        VenueRepository venueRepository
    ) {
        this.bookingService = bookingService;
        this.paymentRecordRepository = paymentRecordRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.venueRepository = venueRepository;
    }

    public PaymentCheckoutResponse createCheckoutSession(PaymentCheckoutRequest request) throws StripeException {
        Stripe.apiKey = secretKey;

        UUID resolvedVenueId = resolveVenueId(request.getVenueId());
        UUID resolvedUserId = resolveUserId(request.getUserId());
        if (resolvedVenueId == null || resolvedUserId == null) {
            throw new IllegalArgumentException("Invalid user or venue id");
        }

        Venue venue = venueRepository.findById(resolvedVenueId)
            .orElseThrow(() -> new IllegalArgumentException("Venue not found"));
        Long amount = normalizeVenueAmount(venue.getPrice());
        validateMinimumAmount(request.getCurrency(), amount);
        

        String orderId = request.getOrderId() == null || request.getOrderId().isBlank()
            ? UUID.randomUUID().toString()
            : request.getOrderId();

        PaymentRecord existing = paymentRecordRepository.findByOrderId(orderId).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.PENDING) {
            if (existing.getCheckoutSessionId() != null && !existing.getCheckoutSessionId().isBlank()) {
                Session existingSession = getCheckoutSession(existing.getCheckoutSessionId());
                PaymentCheckoutResponse resp = new PaymentCheckoutResponse();
                resp.setOrderId(orderId);
                resp.setCheckoutSessionId(existing.getCheckoutSessionId());
                resp.setUrl(existingSession.getUrl());
                return resp;
            }
            PaymentCheckoutResponse resp = new PaymentCheckoutResponse();
            resp.setOrderId(orderId);
            resp.setCheckoutSessionId(existing.getCheckoutSessionId());
            resp.setUrl(existing.getCheckoutSessionId());
            return resp;
        }

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/payment-cancel")
            .setClientReferenceId(orderId)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency(request.getCurrency())
                            .setUnitAmount(amount)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Venue Booking: " + request.getVenueId())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putMetadata("orderId", orderId)
            .putMetadata("venueId", request.getVenueId())
            .putMetadata("userId", request.getUserId())
            .putMetadata("bookingDate", request.getBookingDate())
            .putMetadata("bookingSlot", request.getBookingSlot())
            .build();

        RequestOptions options = RequestOptions.builder()
            .setIdempotencyKey(orderId)
            .build();

        Session session = Session.create(params, options);

        PaymentRecord record = new PaymentRecord();
        record.setOrderId(orderId);
        record.setCheckoutSessionId(session.getId());
        record.setPaymentIntentId(session.getPaymentIntent());
        record.setAmount(amount);
        record.setCurrency(request.getCurrency());
        record.setVenueId(resolvedVenueId);
        record.setUserId(resolvedUserId);
        record.setCourtName(venue.getCourtName());
        record.setCourtLocation(venue.getCourtLocation());
        record.setCourtImage(venue.getCourtImage());
        record.setSport(venue.getSport());
        record.setBookingDate(request.getBookingDate());
        record.setBookingSlot(request.getBookingSlot());
        record.setMembersJoined(1);
        record.setTotalMembers(1);
        record.setStatus(PaymentStatus.PENDING);
        paymentRecordRepository.save(record);

        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(request.getUserId());
        bookingRequest.setVenueId(request.getVenueId());
        bookingRequest.setBookingDate(request.getBookingDate());
        bookingRequest.setBookingSlot(request.getBookingSlot());
        bookingRequest.setOrderId(orderId);
        bookingRequest.setPaymentIntentId(session.getPaymentIntent());
        bookingService.createPendingBooking(bookingRequest);

        PaymentCheckoutResponse response = new PaymentCheckoutResponse();
        response.setOrderId(orderId);
        response.setCheckoutSessionId(session.getId());
        response.setUrl(session.getUrl());
        return response;
    }

    public PaymentCheckoutResponse createCheckoutSessionForBooking(com.example.backend.model.Booking booking)
        throws StripeException {
        Stripe.apiKey = secretKey;

        Venue venue = venueRepository.findById(booking.getVenueId())
            .orElseThrow(() -> new IllegalArgumentException("Venue not found"));
        Long amount = normalizeVenueAmount(venue.getPrice());
        validateMinimumAmount("inr", amount);

        String orderId = booking.getOrderId();
        if (orderId == null || orderId.isBlank()) {
            orderId = UUID.randomUUID().toString();
        }

        PaymentRecord existing = paymentRecordRepository.findByOrderId(orderId).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.PENDING) {
            if (existing.getCheckoutSessionId() != null && !existing.getCheckoutSessionId().isBlank()) {
                Session existingSession = getCheckoutSession(existing.getCheckoutSessionId());
                PaymentCheckoutResponse resp = new PaymentCheckoutResponse();
                resp.setOrderId(orderId);
                resp.setCheckoutSessionId(existing.getCheckoutSessionId());
                resp.setUrl(existingSession.getUrl());
                return resp;
            }
        }

        SessionCreateParams params = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setSuccessUrl(baseUrl + "/payment-success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl(baseUrl + "/payment-cancel")
            .setClientReferenceId(orderId)
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("inr")
                            .setUnitAmount(amount)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName("Venue Booking: " + booking.getVenueId())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .putMetadata("orderId", orderId)
            .putMetadata("venueId", String.valueOf(booking.getVenueId()))
            .putMetadata("userId", String.valueOf(booking.getUserId()))
            .putMetadata("bookingDate", booking.getBookingDate())
            .putMetadata("bookingSlot", booking.getBookingSlot())
            .build();

        RequestOptions options = RequestOptions.builder()
            .setIdempotencyKey(orderId)
            .build();

        Session session = Session.create(params, options);

        PaymentRecord record = existing != null ? existing : new PaymentRecord();
        record.setOrderId(orderId);
        record.setCheckoutSessionId(session.getId());
        record.setPaymentIntentId(session.getPaymentIntent());
        record.setAmount(amount);
        record.setCurrency("inr");
        record.setVenueId(booking.getVenueId());
        record.setUserId(booking.getUserId());
        record.setCourtName(venue.getCourtName());
        record.setCourtLocation(venue.getCourtLocation());
        record.setCourtImage(venue.getCourtImage());
        record.setSport(venue.getSport());
        record.setBookingDate(booking.getBookingDate());
        record.setBookingSlot(booking.getBookingSlot());
        record.setMembersJoined(1);
        record.setTotalMembers(1);
        record.setStatus(PaymentStatus.PENDING);
        paymentRecordRepository.save(record);

        booking.setOrderId(orderId);
        booking.setPaymentIntentId(session.getPaymentIntent());
        bookingRepository.save(booking);

        PaymentCheckoutResponse response = new PaymentCheckoutResponse();
        response.setOrderId(orderId);
        response.setCheckoutSessionId(session.getId());
        response.setUrl(session.getUrl());
        return response;
    }

    public PaymentRecord getPaymentRecord(String orderId) {
        return paymentRecordRepository.findByOrderId(orderId).orElse(null);
    }

    public Session getCheckoutSession(String sessionId) throws StripeException {
        Stripe.apiKey = secretKey;
        return Session.retrieve(sessionId);
    }

    @Transactional
    public void markPaid(String orderId, String paymentIntentId) {
        PaymentRecord record = paymentRecordRepository.findByOrderId(orderId).orElse(null);
        if (record != null) {
            record.setStatus(PaymentStatus.SUCCEEDED);
            record.setPaymentIntentId(paymentIntentId);
            paymentRecordRepository.save(record);
        }
    }

    @Transactional
    public void markFailed(String orderId) {
        PaymentRecord record = paymentRecordRepository.findByOrderId(orderId).orElse(null);
        if (record != null) {
            record.setStatus(PaymentStatus.FAILED);
            paymentRecordRepository.save(record);
        }
    }

    public Refund refundPaymentIntent(String paymentIntentId) throws StripeException {
        Stripe.apiKey = secretKey;
        RefundCreateParams params = RefundCreateParams.builder()
            .setPaymentIntent(paymentIntentId)
            .build();
        return Refund.create(params);
    }

    public List<PaymentRecord> listPaymentsForUser(String userIdOrEmail) {
        UUID userId = resolveUserId(userIdOrEmail);
        if (userId == null) {
            return List.of();
        }
        return paymentRecordRepository.findByUserIdOrderByCreatedAtDesc(userId);
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

    private Long normalizeVenueAmount(Integer price) {
        if (price == null) {
            return 0L;
        }
        if (price < 1000) {
            return price.longValue() * 100;
        }
        return price.longValue();
    }

    private void validateMinimumAmount(String currency, Long amount) {
        if (currency == null || currency.isBlank() || amount == null) {
            return;
        }
        String normalized = currency.trim().toLowerCase();
        long minUnitAmount = "inr".equals(normalized) ? 5000L : 50L;
        if (amount < minUnitAmount) {
            String minDisplay = "inr".equals(normalized)
                ? "Rs 50.00"
                : "0.50 " + currency.toUpperCase();
            throw new IllegalArgumentException("Amount too small. Stripe minimum is " + minDisplay + ".");
        }
    }
}
