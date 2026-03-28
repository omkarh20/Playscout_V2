package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.backend.enums.JoinRequestStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequestResponse {
    @JsonProperty("_id")
    private UUID id;

    @JsonProperty("gameID")
    private UUID gameId;

    @JsonProperty("senderID")
    private UUID senderId;

    private String senderName;
    private String senderEmail;
    private String senderImage;

    @JsonProperty("recipientID")
    private UUID recipientId;

    private JoinRequestStatus status;
    private LocalDateTime createdAt;
}
