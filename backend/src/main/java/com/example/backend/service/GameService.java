package com.example.backend.service;

import com.example.backend.dto.GameResponse;
import com.example.backend.repository.GameRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GameService {
    
    private final GameRepository gameRepository;

    public List<GameResponse> getGameList() {
        return gameRepository.findAll().stream()
                .map(game -> new GameResponse(
                    game.getId(),
                    game.getDate(),
                    game.getSportIcon(),
                    game.getSportName(),
                    game.getCreatedBy().getId(),
                    game.getMembersJoined(),
                    game.getTotalMembers(),
                    game.getSkillLevel(),
                    game.getCourtName(),
                    game.getLocation(),
                    game.getCreatedBy().getName(),
                    game.getCreatedBy().getUserImage() != null 
                        ? game.getCreatedBy().getUserImage() 
                        : "avatars/m_avatar2.png"
                ))
                .toList();
    }

}
