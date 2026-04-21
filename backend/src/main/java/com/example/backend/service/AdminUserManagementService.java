package com.example.backend.service;

import com.example.backend.dto.AdminUserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminUserManagementService {

    private final UserRepository userRepository;

    public long getTotalUsers() {
        return userRepository.count();
    }

    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public void suspendUser(UUID userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setSuspended(!user.getSuspended());
        userRepository.save(user);
    }

    private AdminUserResponse toResponse(User user) {
        AdminUserResponse response = new AdminUserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setSuspended(Boolean.TRUE.equals(user.getSuspended()));
        return response;
    }
}
