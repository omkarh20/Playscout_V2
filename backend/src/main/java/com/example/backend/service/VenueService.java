package com.example.backend.service;

import com.example.backend.dto.VenueResponse;
import com.example.backend.model.Venue;
import com.example.backend.repository.VenueRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueService {

    private final VenueRepository venueRepository;

    public List<VenueResponse> getVenueList() {
        return venueRepository.findAll().stream().map(this::toVenueResponse).toList();
    }

    private VenueResponse toVenueResponse(Venue venue) {
        return new VenueResponse(
                venue.getId(),
                venue.getCourtName(),
                venue.getSport(),
                venue.getCourtLocation(),
                venue.getCourtsAvailable(),
                venue.getPrice(),
                venue.getCourtImage(),
                venue.getGameIcon(),
                venue.getRating());
    }
}
