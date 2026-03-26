package com.example.backend.dto;

import com.example.backend.enums.Role;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private UUID userId;
    private String name;
    private String email;
    private Role role;
    private String userImage;
    private String token;
}