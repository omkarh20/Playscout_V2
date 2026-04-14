package com.example.backend.dto;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.backend.enums.RefundStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminRefundRequestResponse {
    private UUID bookingId;
    private UUID userId;
    private String userEmail;
    private String courtName;
    private String courtLocation;
    private String sport;
    private String bookingDate;
    private String bookingSlot;
    private Integer price;
    private RefundStatus refundStatus;
    private OffsetDateTime refundRequestedAt;
}
