package com.example.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookingResponse {
    @JsonProperty("_id")
    private UUID id;

    @JsonProperty("userID")
    private UUID userId;

    @JsonProperty("venueID")
    private UUID venueId;

    private String courtName;
    private String courtLocation;
    private String courtImage;
    private String sport;
    private LocalDate bookingDate;
    private String slot;
    private BookingStatus status;
    private Integer totalMembers;
    private Integer membersJoined;
    private LocalDateTime createdAt;
}
