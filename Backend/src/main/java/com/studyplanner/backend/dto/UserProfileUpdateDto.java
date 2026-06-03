package com.studyplanner.backend.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Builder

// DTO for profile updates (request) and user data responses (login, get
// profile).
// Email is read-only in responses; cannot be updated via this DTO.
public class UserProfileUpdateDto {
    @NotNull(message = "User ID is required")
    private Long userId;
    private String firstName;
    private String lastName;
    private String email; // Read-only in response; never updated
    private String profilePicture;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
