package com.example.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.enums.JoinRequestStatus;
import com.example.backend.model.JoinRequest;

public interface JoinRequestRepository extends JpaRepository<JoinRequest, UUID> {
    List<JoinRequest> findByRecipientId_IdOrderByCreatedAtDesc(UUID currentUserId);

    List<JoinRequest> findByRecipientId_IdAndStatusOrderByCreatedAtDesc(UUID currentUserId, JoinRequestStatus status);

    List<JoinRequest> findBySenderId_IdOrderByCreatedAtDesc(UUID currentUserId);

    List<JoinRequest> findBySenderId_IdAndStatusOrderByCreatedAtDesc(UUID currentUserId, JoinRequestStatus status);

    Optional<JoinRequest> findByIdAndRecipientId_Id(UUID requestId, UUID currentUserId);

    Optional<JoinRequest> findByIdAndSenderId_Id(UUID requestId, UUID currentUserId);

    boolean existsBySenderId_IdAndGameId_IdAndStatus(UUID senderId, UUID gameId, JoinRequestStatus status);
}
