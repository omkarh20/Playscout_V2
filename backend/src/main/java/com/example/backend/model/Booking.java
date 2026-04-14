package com.example.backend.model;

import com.example.backend.enums.BookingStatus;
import com.example.backend.enums.RefundStatus;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "venue_id", nullable = false)
    private UUID venueId;

    @Column(nullable = false)
    private String bookingDate;

    @Column(nullable = false)
    private String bookingSlot;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus = RefundStatus.NONE;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "refund_id")
    private String refundId;

    @Column(name = "refund_requested_at")
    private OffsetDateTime refundRequestedAt;

    @Column(name = "refund_processed_at")
    private OffsetDateTime refundProcessedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
