package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VenueResponse {

    @JsonProperty("_id")
    private UUID id;

    private String courtName;
    private String sport;
    private String courtLocation;
    private Integer courtsAvailable;
    private Integer price;
    private String courtImage;

    @JsonProperty("game_icon")
    private String gameIcon;

    private BigDecimal rating;
}
