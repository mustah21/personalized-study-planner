package com.studyplanner.backend.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.repository.UserRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    // extreact the currently authenticated user's email from the security context

    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedAccessException("User is not Authenticated");
        }

        // return the email of the user from JWT token
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Use Your Own Address"));
    }

    // return just the user id of the currently authenticated user
    public Long getAuthenticatedUserId() {
        return getAuthenticatedUser().getId();
    }

}
