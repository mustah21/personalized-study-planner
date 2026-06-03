package com.studyplanner.backend.config;

import java.util.List;

import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.service.impl.CalendarServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.studyplanner.backend.security.JwtAuthFilter;
import com.studyplanner.backend.security.OAuth2SuccessHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    @SuppressWarnings("java:S4502") // CSRF disabled intentionally: app is stateless, auth via JWT Bearer tokens in
                                    // headers (not cookies), so CSRF attacks are not applicable
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {
                })
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/v1/users/register",
                                "/api/v1/users/login",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/actuator/**",
                                "/")
                        .permitAll()
                        .requestMatchers("/api/llm/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    @RequestScope
    public CalendarServiceImpl calendarService(OAuth2AuthorizedClientService clientService,
            UserRepository userRepository) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String accessToken = null;

        if (authentication.getClass().isAssignableFrom(OAuth2AuthenticationToken.class)) {
            // OAuth2 login flow
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
            String clientRegistrationId = oauthToken.getAuthorizedClientRegistrationId();
            if (clientRegistrationId.equals("google")) {
                OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(clientRegistrationId,
                        oauthToken.getName());
                accessToken = client.getAccessToken().getTokenValue();
            }
        } else {
            // JWT flow - load token from DB
            String email = authentication.getName();
            userRepository.findByEmail(email).ifPresent(user -> {

                // can't set local var directly, use workaround below
            });
            accessToken = userRepository.findByEmail(email)
                    .map(User::getGoogleAccessToken)
                    .orElse(null);
        }

        return new CalendarServiceImpl(accessToken);
    }
}