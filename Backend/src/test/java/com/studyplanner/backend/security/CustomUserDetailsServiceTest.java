package com.studyplanner.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        service = new CustomUserDetailsService(userRepository);
    }

    @Test
    void loadUserByUsername_WithPassword_ShouldReturnUserDetails() {
        User user = User.builder().email("user@example.com").password("hash").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@example.com");
        assertEquals("user@example.com", details.getUsername());
        assertEquals("hash", details.getPassword());
    }

    @Test
    void loadUserByUsername_WithNullPassword_ShouldUseEmptyPassword() {
        User user = User.builder().email("user@example.com").password(null).build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@example.com");
        assertEquals("", details.getPassword());
    }

    @Test
    void loadUserByUsername_WhenMissing_ShouldThrow() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("missing@example.com"));
    }
}
