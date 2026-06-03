package com.studyplanner.backend.mapper;

import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.entity.User;

public class UserMapper {
    private UserMapper() {
        /* This utility class should not be instantiated */
    }


    // Use when: user logs in, views profile, or fetches user data.
    // Never includes password for security.

    public static UserProfileUpdateDto mapToUserDto(User user) {
        return UserProfileUpdateDto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // Use when: user signs up / registers. Right before userRepository.save(user).

    public static User mapToUser(UserRegisterDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .password(dto.getPassword())
                .firstName(null)
                .lastName(null)
                .profilePicture(null)
                .createdAt(null)
                .updatedAt(null)
                .build();
    }

    public static void applyProfileUpdate(User user, UserProfileUpdateDto dto) {
        if (dto.getFirstName() != null) {
            user.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null) {
            user.setLastName(dto.getLastName());
        }
        if (dto.getProfilePicture() != null) {
            user.setProfilePicture(dto.getProfilePicture());
        }
    }
}
