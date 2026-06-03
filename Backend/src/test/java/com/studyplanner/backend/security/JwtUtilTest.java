package com.studyplanner.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@DisplayName("JwtUtil Tests")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField("secret", "abcdefghijklmnopqrstuvwxyz1234567890abcdefghijklmnopqrstuvwxyz");
        setField("expirationMs", 3600_000L);
    }

    @Test
    void generateAndExtract_ShouldRoundTripEmailAndUserId() {
        UserDetails details = User.withUsername("user@example.com").password("x").authorities("ROLE_USER").build();
        String token = jwtUtil.generateToken(Map.of("userId", 7L), details);

        assertNotNull(token);
        assertEquals("user@example.com", jwtUtil.extractEmail(token));
        assertEquals(7L, jwtUtil.extractUserId(token));
        assertTrue(jwtUtil.validateToken(token));
        assertTrue(jwtUtil.validateToken(token, details));
    }

    @Test
    void validateToken_WithWrongUser_ShouldReturnFalse() {
        UserDetails owner = User.withUsername("owner@example.com").password("x").authorities("ROLE_USER").build();
        UserDetails another = User.withUsername("another@example.com").password("x").authorities("ROLE_USER").build();
        String token = jwtUtil.generateToken(Map.of("userId", 9L), owner);

        assertFalse(jwtUtil.validateToken(token, another));
    }

    @Test
    void validateToken_WithMalformedToken_ShouldReturnFalse() {
        assertFalse(jwtUtil.validateToken("not-a-jwt"));
    }

    private void setField(String name, Object value) throws Exception {
        Field field = JwtUtil.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(jwtUtil, value);
    }
}
