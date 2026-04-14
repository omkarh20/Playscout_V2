package com.example.backend.dto;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminVenueResponse {
    private UUID id;
    private String courtName;
    private String sport;
    private String courtLocation;
    private UUID createdBy;
    private String managerName;
    private Boolean disabled;
    private Integer price;
}
