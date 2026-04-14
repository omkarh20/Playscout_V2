package com.example.backend.model;

import com.example.backend.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(name = "checkout_session_id")
    private String checkoutSessionId;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private String currency;

    @Column(name = "venue_id", nullable = false)
    private UUID venueId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String courtName;

    @Column(nullable = false)
    private String courtLocation;

    @Column(nullable = true)
    private String courtImage;

    @Column(nullable = false)
    private String sport;

    @Column(nullable = false)
    private String bookingDate;

    @Column(nullable = false)
    private String bookingSlot;

    @Column(nullable = false)
    private Integer membersJoined;

    @Column(nullable = false)
    private Integer totalMembers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
