package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.CreateJoinRequestRequest;
import com.example.backend.dto.JoinRequestResponse;
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
}
