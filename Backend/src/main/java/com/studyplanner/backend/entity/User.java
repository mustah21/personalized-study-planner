package com.studyplanner.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// using lombok as it automatically generates getters and setters

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Data

// The @Builder annotation allows us to use the builder pattern to create
// instances of UserDto in a more readable and flexible way.
@Builder

public class User {

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Task> tasks;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SuggestedLLM> suggestedTasks;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private Long id;

    // public Id for safe exposure to the client, not the database ID
    @Column(name = "public_id", unique = true, nullable = false, updatable = false)
    private String publicId;

    @Column(name = "first_name")
    private String firstName;
    
    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password_hash")
    private String password;

    @Column(name = "google_id", unique = true)
    private String googleId; // Google OAuth2 unique identifier for the user

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private AuthProvider authProvider; // LOCAL or GOOGLE

    public enum AuthProvider {
        LOCAL, // for email/password signups
        GOOGLE // for Google OAuth2 signups
    }

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "google_access_token", length = 2048)
    private String googleAccessToken;

    // auto generate publicId when creating a new user
    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString();
        }
    }

}
