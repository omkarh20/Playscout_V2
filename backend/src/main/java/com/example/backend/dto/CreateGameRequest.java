package com.example.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateGameRequest {
    private UUID venueId;
    private LocalDate date;
    private String slot;
    private Integer totalMembers;
    private Integer membersJoined;
    private String skillLevel;
}
