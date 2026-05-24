package com.example.backend.controller;

import com.example.backend.dto.CreateVenueRequest;
import com.example.backend.dto.VenueResponse;
import com.example.backend.service.VenueService;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getVenueList() {
        return ResponseEntity.ok(Map.of("success", true, "data", venueService.getVenueList()));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('FACILITY_MANAGER')")
    public ResponseEntity<Map<String, Object>> getMyVenueList(Authentication authentication) {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", venueService.getVenueListForManager(authentication.getName())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('FACILITY_MANAGER')")
    public ResponseEntity<Map<String, Object>> createVenue(
            Authentication authentication,
            @RequestParam String courtName,
            @RequestParam String sport,
            @RequestParam String courtLocation,
            @RequestParam Integer courtsAvailable,
            @RequestParam Integer price,
            @RequestParam("court-image") MultipartFile courtImage) {
        try {
            CreateVenueRequest request = new CreateVenueRequest();
            request.setCourtName(courtName);
            request.setSport(sport);
            request.setCourtLocation(courtLocation);
            request.setCourtsAvailable(courtsAvailable);
            request.setPrice(price);

            VenueResponse venue = venueService.createVenue(
                    authentication.getName(),
                    request,
                    courtImage);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Venue added successfully", "data", venue));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FACILITY_MANAGER')")
    public ResponseEntity<Map<String, Object>> removeVenue(
            Authentication authentication,
            @PathVariable UUID id) {
        try {
            venueService.deleteVenue(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Venue removed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
