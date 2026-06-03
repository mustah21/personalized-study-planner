package com.studyplanner.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    void builderAndGetters() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 7, 8, 9);
        User u = User.builder()
                .id(42L)
                .publicId("pub-1")
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .password("hashed")
                .googleId("g-123")
                .authProvider(User.AuthProvider.GOOGLE)
                .profilePicture("/img.png")
                .createdAt(now)
                .updatedAt(now)
                .googleAccessToken("token")
                .build();

        assertEquals(42L, u.getId());
        assertEquals("pub-1", u.getPublicId());
        assertEquals("Jane", u.getFirstName());
        assertEquals("Doe", u.getLastName());
        assertEquals("jane.doe@example.com", u.getEmail());
        assertEquals("hashed", u.getPassword());
        assertEquals("g-123", u.getGoogleId());
        assertEquals(User.AuthProvider.GOOGLE, u.getAuthProvider());
        assertEquals("/img.png", u.getProfilePicture());
        assertEquals(now, u.getCreatedAt());
        assertEquals(now, u.getUpdatedAt());
        assertEquals("token", u.getGoogleAccessToken());
    }

    @Test
    void enumContainsExpectedValues() {
        User.AuthProvider[] providers = User.AuthProvider.values();
        assertEquals(2, providers.length);
        assertArrayEquals(new User.AuthProvider[]{User.AuthProvider.LOCAL, User.AuthProvider.GOOGLE}, providers);
    }

    @Test
    void generatePublicId_setsWhenNull() {
        User u = new User();
        u.setPublicId(null);
        u.generatePublicId();
        assertNotNull(u.getPublicId());
        assertFalse(u.getPublicId().isEmpty());
    }

    @Test
    void generatePublicId_preservesExistingValue() {
        User u = new User();
        u.setPublicId("existing-id");
        u.generatePublicId();
        assertEquals("existing-id", u.getPublicId());
    }

    @Test
    void equalsAndHashCode_consistentForSameData() {
        User a = User.builder().id(7L).email("a@x.com").build();
        User b = User.builder().id(7L).email("a@x.com").build();
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_containsClassName() {
        User u = User.builder().id(3L).email("t@t.com").build();
        assertTrue(u.toString().contains("User"));
    }
}

