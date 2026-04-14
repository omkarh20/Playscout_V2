package com.example.backend.controller;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.CreateGameRequest;
import com.example.backend.dto.GameResponse;
import com.example.backend.factory.VenueActionFactory;
import com.example.backend.factory.VenueActionHandler;
import com.example.backend.factory.VenueActionType;
import com.example.backend.service.GameService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {
    
    private final GameService gameService;
    private final VenueActionFactory venueActionFactory;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGameList() {
        return ResponseEntity.ok(Map.of("success", true, "data", gameService.getGameList()));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Map<String, Object>> getMyGameList(Authentication authentication) {
        try {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", gameService.getMyGames(authentication.getName())));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Map<String, Object>> createGame(
            Authentication authentication,
            @RequestBody CreateGameRequest request) {
        try {
            VenueActionHandler<CreateGameRequest, GameResponse> createHandler =
                    venueActionFactory.getHandler(VenueActionType.CREATE, CreateGameRequest.class);
            GameResponse game = createHandler.execute(authentication.getName(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Game created successfully", "data", game));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PLAYER')")
    public ResponseEntity<Map<String, Object>> removeGame(
            Authentication authentication,
            @PathVariable UUID id) {
        try {
            gameService.removeGame(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Game removed successfully"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
