package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {
    private String userId;

    @NotBlank
    private String venueId;

    @NotBlank
    private String bookingDate;

    @NotBlank
    private String bookingSlot;

    private String orderId;

    private String paymentIntentId;
}
