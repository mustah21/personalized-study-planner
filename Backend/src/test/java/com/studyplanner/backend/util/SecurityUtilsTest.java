package com.studyplanner.backend.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityUtils Tests")
class SecurityUtilsTest {

    @Mock
    private UserRepository userRepository;

    private SecurityUtils securityUtils;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        securityUtils = new SecurityUtils(userRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAuthenticatedUser_ShouldReturnUserAndId() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("me@example.com", "x", java.util.List.of()));
        User user = User.builder().id(99L).email("me@example.com").build();
        when(userRepository.findByEmail("me@example.com")).thenReturn(Optional.of(user));

        assertEquals(99L, securityUtils.getAuthenticatedUser().getId());
        assertEquals(99L, securityUtils.getAuthenticatedUserId());
    }

    @Test
    void getAuthenticatedUser_WhenMissingOrUnauthenticated_ShouldThrow() {
        assertThrows(UnauthorizedAccessException.class, () -> securityUtils.getAuthenticatedUser());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("x@example.com", "x", java.util.List.of()));
        when(userRepository.findByEmail("x@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> securityUtils.getAuthenticatedUser());
    }
}
