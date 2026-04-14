package com.example.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.backend.model.PaymentRecord;
import com.example.backend.service.BookingService;
import com.example.backend.service.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@RestController
public class StripeWebhookController {

    private static final Logger log = LoggerFactory.getLogger(StripeWebhookController.class);

    @Value("${stripe.webhookSecret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final BookingService bookingService;

    public StripeWebhookController(PaymentService paymentService, BookingService bookingService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
    }

    @PostMapping("/webhooks/stripe")
    public ResponseEntity<String> handleStripeWebhook(
        @RequestBody String payload,
        @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            String type = event.getType();
            log.info("Stripe webhook received: type={}", type);

            if ("checkout.session.completed".equals(type)) {
                Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if (session != null && session.getMetadata() != null) {
                    String orderId = session.getMetadata().get("orderId");
                    log.info("Stripe checkout completed: orderId={}, sessionId={}", orderId, session.getId());
                    paymentService.markPaid(orderId, session.getPaymentIntent());

                    PaymentRecord paymentRecord = paymentService.getPaymentRecord(orderId);
                    com.example.backend.dto.BookingRequest bookingRequest = new com.example.backend.dto.BookingRequest();

                    if (paymentRecord != null) {
                        bookingRequest.setUserId(paymentRecord.getUserId() != null ? paymentRecord.getUserId().toString() : null);
                        bookingRequest.setVenueId(paymentRecord.getVenueId() != null ? paymentRecord.getVenueId().toString() : null);
                        bookingRequest.setBookingDate(paymentRecord.getBookingDate());
                        bookingRequest.setBookingSlot(paymentRecord.getBookingSlot());
                        bookingRequest.setOrderId(paymentRecord.getOrderId());
                        bookingRequest.setPaymentIntentId(paymentRecord.getPaymentIntentId());
                    } else {
                        bookingRequest.setUserId(session.getMetadata().get("userId"));
                        bookingRequest.setVenueId(session.getMetadata().get("venueId"));
                        bookingRequest.setBookingDate(session.getMetadata().get("bookingDate"));
                        bookingRequest.setBookingSlot(session.getMetadata().get("bookingSlot"));
                        bookingRequest.setOrderId(session.getMetadata().get("orderId"));
                        bookingRequest.setPaymentIntentId(session.getPaymentIntent());
                    }

                    bookingService.confirmOrCreateBooking(bookingRequest);
                    log.info("Booking created from webhook: orderId={}", orderId);
                }
            } else if ("payment_intent.payment_failed".equals(type)) {
                if (event.getDataObjectDeserializer().getObject().isPresent()) {
                    Session session = (Session) event.getDataObjectDeserializer().getObject().get();
                    if (session.getMetadata() != null) {
                        String orderId = session.getMetadata().get("orderId");
                        paymentService.markFailed(orderId);
                        log.warn("Stripe payment failed: orderId={}", orderId);
                    }
                }
            }

            return ResponseEntity.ok("ok");
        } catch (SignatureVerificationException e) {
            log.warn("Stripe webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body("invalid signature");
        } catch (RuntimeException e) {
            log.error("Stripe webhook processing failed", e);
            return ResponseEntity.internalServerError().body("webhook error");
        }
    }
}
