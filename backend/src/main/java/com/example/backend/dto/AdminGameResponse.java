package com.example.backend.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminGameResponse {
    private UUID id;
    private String courtName;
    private String sport;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String creatorName;
    private UUID createdById;
    private String status;
    private Integer totalMembers;
    private Integer membersJoined;
}
