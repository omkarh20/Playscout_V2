package com.example.backend.factory;

import com.example.backend.dto.CreateGameRequest;
import com.example.backend.dto.GameResponse;
import com.example.backend.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateGameVenueActionHandler implements VenueActionHandler<CreateGameRequest, GameResponse> {

    private final GameService gameService;

    @Override
    public VenueActionType getActionType() {
        return VenueActionType.CREATE;
    }

    @Override
    public Class<CreateGameRequest> getRequestType() {
        return CreateGameRequest.class;
    }

    @Override
    public GameResponse execute(String actor, CreateGameRequest request) {
        if (actor == null || actor.isBlank()) {
            throw new IllegalArgumentException("Authenticated user is required");
        }
        return gameService.createGame(actor, request);
    }
}
