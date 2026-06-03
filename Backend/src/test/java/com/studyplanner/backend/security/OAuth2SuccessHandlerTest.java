package com.studyplanner.backend.security;

import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock private OAuth2AuthorizedClientService authorizedClientService;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserRepository userRepository;
    @Mock private UserDetailsService userDetailsService;
    @Mock private EmailService emailService;

    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;

    @InjectMocks
    private OAuth2SuccessHandler successHandler;

    private OAuth2User oauth2User;
    private OAuth2AuthenticationToken authentication;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(successHandler, "frontendUrl", "http://localhost:3000");

        oauth2User = mock(OAuth2User.class);
        when(oauth2User.getAttribute("email")).thenReturn("test@example.com");
        when(oauth2User.getAttribute("sub")).thenReturn("google-123");
        when(oauth2User.getAttribute("name")).thenReturn("John Doe");

        authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(authentication.getName()).thenReturn("google-123");
        when(authentication.getAuthorizedClientRegistrationId()).thenReturn("google");

        // Keep redirect strategy deterministic in unit tests.
        lenient().when(response.encodeRedirectURL(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldCreateNewUserAndRedirect() throws IOException {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .firstName("John Doe")
                .googleId("google-123")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(anyMap(), any())).thenReturn("mock-jwt-test@example.com");

        mockOAuthClient();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());

        String redirectUrl = captor.getValue();
        assertNotNull(redirectUrl, "Redirect URL should not be null");
        assertTrue(redirectUrl.contains("mock-jwt-test@example.com"));
        verify(emailService).sendWelcomeEmail("test@example.com", "John Doe");
    }

    @Test
    void shouldLinkExistingEmailUserToGoogle() throws IOException {
        User existing = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenReturn(existing);

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(anyMap(), any())).thenReturn("mock-jwt-test@example.com");

        mockOAuthClient();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository, atLeastOnce()).save(any(User.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).sendRedirect(captor.capture());

        assertNotNull(captor.getValue());
        assertTrue(captor.getValue().contains("mock-jwt-test@example.com"));
    }

    @Test
    void shouldContinueWhenWelcomeEmailFails() throws IOException {
        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(9L);
            return u;
        });
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(anyMap(), any())).thenReturn("mock-jwt");
        org.mockito.Mockito.doThrow(new RuntimeException("smtp down"))
                .when(emailService).sendWelcomeEmail("test@example.com","John Doe");
        mockOAuthClient();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect(contains("mock-jwt"));
    }

    @Test
    void shouldUseExistingGoogleUser() throws IOException {
        User existing = User.builder()
                .id(1L)
                .email("test@example.com")
                .googleId("google-123")
                .build();

        when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existing));

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userDetails);
        when(jwtUtil.generateToken(anyMap(), any())).thenReturn("mock-jwt-test@example.com");

        mockOAuthClient();

        successHandler.onAuthenticationSuccess(request, response, authentication);

        // Verification of sendRedirect now works because encodeRedirectURL isn't returning null
        verify(response).sendRedirect(contains("mock-jwt-test@example.com"));
    }

    private void mockOAuthClient() {
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "google-token",
                Instant.now(),
                Instant.now().plusSeconds(3600)
        );

        OAuth2AuthorizedClient client = mock(OAuth2AuthorizedClient.class);
        when(client.getAccessToken()).thenReturn(token);

        lenient().when(authorizedClientService.loadAuthorizedClient(anyString(), anyString()))
                .thenReturn(client);
    }
}