// java
package com.studyplanner.backend.controller;

import com.studyplanner.backend.dto.UserLoginDto;
import com.studyplanner.backend.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authManager,
            UserDetailsService userDetailsService,
            JwtUtil jwtUtil) {
        this.authManager = authManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDto body) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(body.getEmail(), body.getPassword()));
        UserDetails userDetails = userDetailsService.loadUserByUsername(body.getEmail());
        String token = jwtUtil.generateToken(Map.of("userId", userDetails.getUsername()), userDetails);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
