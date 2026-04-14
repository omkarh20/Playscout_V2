package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateVenueRequest {
    private String courtName;
    private String sport;
    private String courtLocation;
    private Integer courtsAvailable;
    private Integer price;
}
