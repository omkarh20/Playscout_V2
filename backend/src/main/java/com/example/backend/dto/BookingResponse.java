package com.example.backend.dto;

import com.example.backend.enums.BookingStatus;
import com.example.backend.enums.RefundStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingResponse {
    private UUID id;
    private UUID userId;
    private UUID venueId;
    private String courtName;
    private String courtLocation;
    private String courtImage;
    private String sport;
    private String bookingDate;
    private String bookingSlot;
    private BookingStatus status;
    private RefundStatus refundStatus;
    private OffsetDateTime createdAt;
}
