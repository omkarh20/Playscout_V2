package com.example.backend.repository;

import com.example.backend.model.Venue;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VenueRepository extends JpaRepository<Venue, UUID> {
}
