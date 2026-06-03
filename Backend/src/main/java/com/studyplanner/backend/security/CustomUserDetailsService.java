package com.studyplanner.backend.security;

import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        String passwordHash = user.getPassword() != null ? user.getPassword() : "";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(passwordHash)
                .authorities("ROLE_USER")
                .build();
    }
}