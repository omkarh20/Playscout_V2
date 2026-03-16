package com.example.backend.controller;

import com.example.backend.service.VenueService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venue")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping("/venue-list")
    public ResponseEntity<Map<String, Object>> getVenueList() {
        return ResponseEntity.ok(Map.of("success", true, "data", venueService.getVenueList()));
    }
}
