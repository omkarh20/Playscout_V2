package com.example.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.backend.dto.CreateJoinRequestRequest;
import com.example.backend.dto.JoinRequestResponse;
import com.example.backend.enums.JoinRequestStatus;
import com.example.backend.service.JoinRequestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/join-requests")
@RequiredArgsConstructor
public class JoinRequestController {
    
    private final JoinRequestService joinRequestService;
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createJoinRequest(
        Authentication authentication,
        @RequestBody CreateJoinRequestRequest request) {
            try {
                JoinRequestResponse response = joinRequestService.createJoinRequest(authentication.getName(), request);
                return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "Join request created successfully", "data", response));
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", ex.getMessage()));
            }
        }

    @GetMapping("/incoming")
    public ResponseEntity<Map<String, Object>> getIncomingRequests(
            Authentication authentication,
            @RequestParam(required = false) JoinRequestStatus status) {
        try {
            List<JoinRequestResponse> responses = joinRequestService.getIncomingRequests(authentication.getName(), status);
            return ResponseEntity.ok(Map.of("success", true, "data", responses));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @GetMapping("/sent")
    public ResponseEntity<Map<String, Object>> getSentRequests(
            Authentication authentication,
            @RequestParam(required = false) JoinRequestStatus status) {
        try {
            List<JoinRequestResponse> responses = joinRequestService.getSentRequests(authentication.getName(), status);
            return ResponseEntity.ok(Map.of("success", true, "data", responses));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<Map<String, Object>> acceptRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        try {
            JoinRequestResponse response = joinRequestService.acceptRequest(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Join request accepted", "data", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        try {
            JoinRequestResponse response = joinRequestService.rejectRequest(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Join request rejected", "data", response));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> cancelSentRequest(
            Authentication authentication,
            @PathVariable UUID id) {
        try {
            joinRequestService.cancelSentRequest(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("success", true, "message", "Join request cancelled"));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        }
    }
}
