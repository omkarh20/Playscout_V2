package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.dto.CreateJoinRequestRequest;
import com.example.backend.dto.JoinRequestResponse;
import com.example.backend.enums.JoinRequestStatus;
import com.example.backend.model.Game;
import com.example.backend.model.JoinRequest;
import com.example.backend.model.User;
import com.example.backend.repository.GameRepository;
import com.example.backend.repository.JoinRequestRepository;
import com.example.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JoinRequestService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
        private final JoinRequestRepository joinRequestRepository;

    public JoinRequestResponse createJoinRequest(String email, CreateJoinRequestRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getMembersJoined() >= game.getTotalMembers()) {
            throw new IllegalArgumentException("Game is already full");
        }

        if (game.getCreatedBy().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You cannot join your own game");
        }

        boolean pendingRequestExists = joinRequestRepository.existsBySenderId_IdAndGameId_IdAndStatus(
                        user.getId(),
                        game.getId(),
                        JoinRequestStatus.PENDING);

        if (pendingRequestExists) {
            throw new IllegalArgumentException("Pending join request already exists for this game");
        }

        boolean acceptedRequestExists = joinRequestRepository.existsBySenderId_IdAndGameId_IdAndStatus(
                        user.getId(),
                        game.getId(),
                        JoinRequestStatus.ACCEPTED);

        if (acceptedRequestExists) {
            throw new IllegalArgumentException("You are already accepted for this game");
        }

        JoinRequest joinRequest = new JoinRequest();
        joinRequest.setSenderId(user);
        joinRequest.setRecipientId(userRepository.findById(game.getCreatedBy().getId())
                .orElseThrow(() -> new IllegalArgumentException("Recipient user not found")));
        joinRequest.setGameId(game);
        joinRequest.setStatus(JoinRequestStatus.PENDING);

        joinRequestRepository.save(joinRequest);
        return toResponse(joinRequest);
    }
    
    private JoinRequestResponse toResponse(JoinRequest joinRequest) {
        JoinRequestResponse response = new JoinRequestResponse();
        response.setId(joinRequest.getId());
        response.setGameId(joinRequest.getGameId().getId());
        response.setSenderId(joinRequest.getSenderId().getId());
        response.setRecipientId(joinRequest.getRecipientId().getId());
        response.setStatus(joinRequest.getStatus());
        response.setCreatedAt(joinRequest.getCreatedAt());
        return response;
    }
}
