package com.example.backend.service;

import java.time.LocalDate;
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
import com.example.backend.util.JoinRequestValidators;
import com.example.backend.util.JoinRequestValidators.GameNotFullValidator;
import com.example.backend.util.JoinRequestValidators.SenderNotCreatorValidator;
import com.example.backend.util.JoinRequestValidators.NoPendingRequestValidator;
import com.example.backend.util.JoinRequestValidators.NotAlreadyAcceptedValidator;

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

        boolean pendingRequestExists = joinRequestRepository.existsBySenderId_IdAndGameId_IdAndStatus(
                        user.getId(),
                        game.getId(),
                        JoinRequestStatus.PENDING);

        boolean acceptedRequestExists = joinRequestRepository.existsBySenderId_IdAndGameId_IdAndStatus(
                        user.getId(),
                        game.getId(),
                        JoinRequestStatus.ACCEPTED);

        // Build validator chain
        JoinRequestValidators validatorChain = new GameNotFullValidator();
        validatorChain.setNext(new SenderNotCreatorValidator())
                .setNext(new NoPendingRequestValidator())
                .setNext(new NotAlreadyAcceptedValidator());

        // Execute chain validation
        validatorChain.validate(user, game, pendingRequestExists, acceptedRequestExists);

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

        if (status == null) {
            return joinRequestRepository.findIncomingRequestsForUpcomingGames(user.getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else {
            return joinRequestRepository.findByRecipientId_IdAndStatusOrderByCreatedAtDesc(user.getId(), status)
                    .stream()
                    .filter(jr -> jr.getGameId().getDate().isAfter(LocalDate.now()) || jr.getGameId().getDate().isEqual(LocalDate.now()))
                    .map(this::toResponse)
                    .toList();
        }
    }

    public List<JoinRequestResponse> getSentRequests(String email, JoinRequestStatus status) {
        User user = getUserByEmail(email);

        if (status == null) {
            return joinRequestRepository.findSentRequestsForUpcomingGames(user.getId())
                    .stream()
                    .map(this::toResponse)
                    .toList();
        } else {
            return joinRequestRepository.findBySenderId_IdAndStatusOrderByCreatedAtDesc(user.getId(), status)
                    .stream()
                    .filter(jr -> jr.getGameId().getDate().isAfter(LocalDate.now()) || jr.getGameId().getDate().isEqual(LocalDate.now()))
                    .map(this::toResponse)
                    .toList();
        }
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
