package com.example.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.backend.enums.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private UUID id;
    private String orderId;
    private String courtName;
    private String courtLocation;
    private String sport;
    private String bookingDate;
    private String bookingSlot;
    private Long amount;
    private String currency;
    private PaymentStatus status;
    private OffsetDateTime createdAt;
}
