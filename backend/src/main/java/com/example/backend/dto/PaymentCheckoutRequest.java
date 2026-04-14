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
public class PaymentCheckoutRequest {
    @NotBlank
    private String currency;

    @NotBlank
    private String venueId;

    @NotBlank
    private String userId;

    @NotBlank
    private String bookingDate;

    @NotBlank
    private String bookingSlot;

    private String orderId;
}
