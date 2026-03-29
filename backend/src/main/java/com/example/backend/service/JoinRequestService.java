package com.example.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static final DateTimeFormatter SLOT_FORMATTER = DateTimeFormatter.ofPattern("H:mm");

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final JoinRequestRepository joinRequestRepository;

    public JoinRequestResponse createJoinRequest(String email, CreateJoinRequestRequest request) {
        if (request == null || request.getGameId() == null) {
            throw new IllegalArgumentException("Game id is required");
        }

        User user = getUserByEmail(email);

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

    public List<JoinRequestResponse> getIncomingRequests(String email, JoinRequestStatus status) {
        User user = getUserByEmail(email);

        List<JoinRequest> joinRequests = status == null
                ? joinRequestRepository.findByRecipientId_IdOrderByCreatedAtDesc(user.getId())
                : joinRequestRepository.findByRecipientId_IdAndStatusOrderByCreatedAtDesc(user.getId(), status);

        return joinRequests.stream()
                .map(this::toResponse)
                .toList();
    }

    public List<JoinRequestResponse> getSentRequests(String email, JoinRequestStatus status) {
        User user = getUserByEmail(email);

        List<JoinRequest> joinRequests = status == null
                ? joinRequestRepository.findBySenderId_IdOrderByCreatedAtDesc(user.getId())
                : joinRequestRepository.findBySenderId_IdAndStatusOrderByCreatedAtDesc(user.getId(), status);

        return joinRequests.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public JoinRequestResponse acceptRequest(String email, UUID requestId) {
        User user = getUserByEmail(email);
        JoinRequest joinRequest = joinRequestRepository.findByIdAndRecipientId_Id(requestId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be accepted");
        }

        Game game = joinRequest.getGameId();
        if (game.getMembersJoined() >= game.getTotalMembers()) {
            throw new IllegalArgumentException("Game is already full");
        }

        joinRequest.setStatus(JoinRequestStatus.ACCEPTED);
        game.setMembersJoined(game.getMembersJoined() + 1);

        gameRepository.save(game);
        joinRequestRepository.save(joinRequest);
        return toResponse(joinRequest);
    }

    @Transactional
    public JoinRequestResponse rejectRequest(String email, UUID requestId) {
        User user = getUserByEmail(email);
        JoinRequest joinRequest = joinRequestRepository.findByIdAndRecipientId_Id(requestId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }

        joinRequest.setStatus(JoinRequestStatus.REJECTED);
        joinRequestRepository.save(joinRequest);
        return toResponse(joinRequest);
    }

    @Transactional
    public void cancelSentRequest(String email, UUID requestId) {
        User user = getUserByEmail(email);
        JoinRequest joinRequest = joinRequestRepository.findByIdAndSenderId_Id(requestId, user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Join request not found"));

        if (joinRequest.getStatus() != JoinRequestStatus.PENDING) {
            throw new IllegalArgumentException("Only pending requests can be cancelled");
        }

        joinRequestRepository.delete(joinRequest);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    
    private JoinRequestResponse toResponse(JoinRequest joinRequest) {
        Game game = joinRequest.getGameId();
        JoinRequestResponse response = new JoinRequestResponse();
        response.setId(joinRequest.getId());
        response.setGameId(game.getId());
        response.setSenderId(joinRequest.getSenderId().getId());
        response.setSenderName(joinRequest.getSenderId().getName());
        response.setSenderEmail(joinRequest.getSenderId().getEmail());
        response.setSenderImage(joinRequest.getSenderId().getUserImage());
        response.setRecipientId(joinRequest.getRecipientId().getId());
        response.setRecipientName(joinRequest.getRecipientId().getName());
        response.setRecipientEmail(joinRequest.getRecipientId().getEmail());
        response.setRecipientImage(joinRequest.getRecipientId().getUserImage());
        response.setGameDate(game.getDate());
        response.setGameSlot(game.getStartTime().format(SLOT_FORMATTER) + "-" + game.getEndTime().format(SLOT_FORMATTER));
        response.setSportName(game.getVenue().getSport());
        response.setSportIcon(game.getVenue().getGameIcon());
        response.setCourtName(game.getVenue().getCourtName());
        response.setLocation(game.getVenue().getCourtLocation());
        response.setMembersJoined(game.getMembersJoined());
        response.setTotalMembers(game.getTotalMembers());
        response.setLevel(game.getSkillLevel());
        response.setStatus(joinRequest.getStatus());
        response.setCreatedAt(joinRequest.getCreatedAt());
        return response;
    }
}
