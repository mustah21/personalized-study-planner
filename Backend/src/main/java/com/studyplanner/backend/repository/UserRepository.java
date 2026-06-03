package com.studyplanner.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studyplanner.backend.entity.User;

// The purpose of this repository is to perform CRUD operations on User entities or models.
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleId(String googleId); // For finding users who signed up via Google OAuth2

    boolean existsByEmail(String email); // Check if a user with the given email already exists

    // Search by name or email (handles null firstName/lastName)
    @Query("SELECT u FROM User u " +
            "WHERE u.id <> :excludeUserId " +
            "AND (" +
            "LOWER(COALESCE(u.firstName, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(COALESCE(u.lastName, ''))  LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(CONCAT(COALESCE(u.firstName, ''), ' ', COALESCE(u.lastName, ''))) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))" +
            ")")
    List<User> searchUsers(@Param("query") String query,
            @Param("excludeUserId") Long excludeUserId);

}
