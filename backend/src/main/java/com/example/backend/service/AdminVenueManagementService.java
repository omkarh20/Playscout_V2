package com.example.backend.service;

import com.example.backend.dto.AdminVenueResponse;
import com.example.backend.model.Venue;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.VenueRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminVenueManagementService {

    private final VenueRepository venueRepository;
    private final UserRepository userRepository;

    public long getTotalVenues() {
        return venueRepository.count();
    }

    public List<AdminVenueResponse> getAllVenues() {
        return venueRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void disableVenue(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
            .orElseThrow(() -> new IllegalArgumentException("Venue not found"));
        venue.setDisabled(!venue.getDisabled());
        venueRepository.save(venue);
    }

    private AdminVenueResponse toResponse(Venue venue) {
        AdminVenueResponse response = new AdminVenueResponse();
        response.setId(venue.getId());
        response.setCourtName(venue.getCourtName());
        response.setSport(venue.getSport());
        response.setCourtLocation(venue.getCourtLocation());
        response.setCreatedBy(venue.getCreatedBy());
        response.setDisabled(Boolean.TRUE.equals(venue.getDisabled()));
        response.setPrice(venue.getPrice());

        if (venue.getCreatedBy() != null) {
            userRepository.findById(venue.getCreatedBy())
                .ifPresent(user -> response.setManagerName(user.getName()));
        }

        return response;
    }
}
