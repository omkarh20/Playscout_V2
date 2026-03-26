package com.example.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    private UUID venueId;
    private LocalDate bookingDate;
    private String slot;
    private Integer totalMembers;
}
