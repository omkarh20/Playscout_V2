package com.example.backend.service;

import com.example.backend.dto.AdminGameResponse;
import com.example.backend.model.Game;
import com.example.backend.repository.GameRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminGameManagementService {

    private final GameRepository gameRepository;

    public long getTotalGames() {
        return gameRepository.count();
    }

    public List<AdminGameResponse> getRecentGames(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"));
        return gameRepository.findAll(pageable).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void cancelGame(UUID gameId) {
        Game game = gameRepository.findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        if ("CANCELLED".equals(game.getStatus())) {
            throw new IllegalArgumentException("Game is already cancelled");
        }
        game.setStatus("CANCELLED");
        gameRepository.save(game);
    }

    private AdminGameResponse toResponse(Game game) {
        AdminGameResponse response = new AdminGameResponse();
        response.setId(game.getId());
        response.setCourtName(game.getVenue().getCourtName());
        response.setSport(game.getVenue().getSport());
        response.setDate(game.getDate());
        response.setStartTime(game.getStartTime());
        response.setEndTime(game.getEndTime());
        response.setCreatorName(game.getCreatedBy().getName());
        response.setCreatedById(game.getCreatedBy().getId());
        response.setStatus(game.getStatus() != null ? game.getStatus() : "ACTIVE");
        response.setTotalMembers(game.getTotalMembers());
        response.setMembersJoined(game.getMembersJoined());
        return response;
    }
}
