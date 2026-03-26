package com.example.backend.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameResponse {
    
    @JsonProperty("_id")
    private UUID id;

    private String date;
    private String sportIcon;
    private String sportName;

    @JsonProperty("userID")
    private UUID createdBy;
    
    private Integer membersJoined;
    private Integer totalMembers;

    @JsonProperty("level")
    private String skillLevel;

    private String courtName;
    private String location;
    private String userName;
    private String userImage;
}
