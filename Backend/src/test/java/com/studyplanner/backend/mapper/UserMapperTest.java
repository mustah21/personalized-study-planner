package com.studyplanner.backend.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.entity.User;

/**
 * Unit tests for UserMapper.
 * Tests cover all mapping operations between User entity and DTOs.
 */
class UserMapperTest {

    private User sampleUser;
    private UserRegisterDto sampleRegisterDto;
    private UserProfileUpdateDto sampleProfileUpdateDto;

    @BeforeEach
    void setUp() {
        // Sample User entity
        sampleUser = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("hashedPassword123")
                .profilePicture("profile.jpg")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 15, 30, 0))
                .build();

        // Sample registration DTO
        sampleRegisterDto = new UserRegisterDto();
        sampleRegisterDto.setEmail("newuser@example.com");
        sampleRegisterDto.setPassword("securePassword123");

        // Sample profile update DTO
        sampleProfileUpdateDto = UserProfileUpdateDto.builder()
                .userId(1L)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@example.com")
                .profilePicture("new-profile.jpg")
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 20, 12, 0, 0))
                .build();
    }

    @Nested
    @DisplayName("mapToUserDto Tests")
    class MapToUserDtoTests {

        @Test
        @DisplayName("Should map all User fields to UserProfileUpdateDto")
        void mapToUserDto_ShouldMapAllFields() {
            // Act
            UserProfileUpdateDto result = UserMapper.mapToUserDto(sampleUser);

            // Assert
            assertNotNull(result);
            assertEquals(sampleUser.getId(), result.getUserId());
            assertEquals(sampleUser.getFirstName(), result.getFirstName());
            assertEquals(sampleUser.getLastName(), result.getLastName());
            assertEquals(sampleUser.getEmail(), result.getEmail());
            assertEquals(sampleUser.getProfilePicture(), result.getProfilePicture());
            assertEquals(sampleUser.getCreatedAt(), result.getCreatedAt());
            assertEquals(sampleUser.getUpdatedAt(), result.getUpdatedAt());
        }

        @Test
        @DisplayName("Should handle null fields in User entity")
        void mapToUserDto_WithNullFields_ShouldHandleGracefully() {
            // Arrange
            User userWithNulls = User.builder()
                    .id(2L)
                    .email("minimal@example.com")
                    .build();

            // Act
            UserProfileUpdateDto result = UserMapper.mapToUserDto(userWithNulls);

            // Assert
            assertNotNull(result);
            assertEquals(2L, result.getUserId());
            assertEquals("minimal@example.com", result.getEmail());
            assertNull(result.getFirstName());
            assertNull(result.getLastName());
            assertNull(result.getProfilePicture());
        }

        @Test
        @DisplayName("Should not expose password in DTO")
        void mapToUserDto_ShouldNotExposePassword() {
            // Act
            UserProfileUpdateDto result = UserMapper.mapToUserDto(sampleUser);

            // Assert - DTO should not have password field
            // The UserProfileUpdateDto class doesn't have a password field by design
            assertNotNull(result);
            // Verifying the DTO was created without password exposure
            assertEquals(sampleUser.getEmail(), result.getEmail());
        }
    }

    @Nested
    @DisplayName("mapToUser Tests")
    class MapToUserTests {

        @Test
        @DisplayName("Should map UserRegisterDto to User entity")
        void mapToUser_ShouldMapEmailAndPassword() {
            // Act
            User result = UserMapper.mapToUser(sampleRegisterDto);

            // Assert
            assertNotNull(result);
            assertEquals(sampleRegisterDto.getEmail(), result.getEmail());
            assertEquals(sampleRegisterDto.getPassword(), result.getPassword());
        }

        @Test
        @DisplayName("Should set optional fields to null")
        void mapToUser_ShouldSetOptionalFieldsToNull() {
            // Act
            User result = UserMapper.mapToUser(sampleRegisterDto);

            // Assert
            assertNull(result.getFirstName());
            assertNull(result.getLastName());
            assertNull(result.getProfilePicture());
            assertNull(result.getCreatedAt());
            assertNull(result.getUpdatedAt());
        }

        @Test
        @DisplayName("Should not set ID for new user")
        void mapToUser_ShouldNotSetId() {
            // Act
            User result = UserMapper.mapToUser(sampleRegisterDto);

            // Assert - ID should be null for new users (set by database)
            assertNull(result.getId());
        }
    }

    @Nested
    @DisplayName("applyProfileUpdate Tests")
    class ApplyProfileUpdateTests {

        @Test
        @DisplayName("Should update all non-null fields")
        void applyProfileUpdate_ShouldUpdateAllProvidedFields() {
            // Act
            UserMapper.applyProfileUpdate(sampleUser, sampleProfileUpdateDto);

            // Assert
            assertEquals("Jane", sampleUser.getFirstName());
            assertEquals("Smith", sampleUser.getLastName());
            assertEquals("new-profile.jpg", sampleUser.getProfilePicture());
        }

        @Test
        @DisplayName("Should not update firstName when null in DTO")
        void applyProfileUpdate_WithNullFirstName_ShouldNotUpdateFirstName() {
            // Arrange
            UserProfileUpdateDto partialDto = UserProfileUpdateDto.builder()
                    .userId(1L)
                    .lastName("NewLastName")
                    .build();

            String originalFirstName = sampleUser.getFirstName();

            // Act
            UserMapper.applyProfileUpdate(sampleUser, partialDto);

            // Assert
            assertEquals(originalFirstName, sampleUser.getFirstName());
            assertEquals("NewLastName", sampleUser.getLastName());
        }

        @Test
        @DisplayName("Should not update lastName when null in DTO")
        void applyProfileUpdate_WithNullLastName_ShouldNotUpdateLastName() {
            // Arrange
            UserProfileUpdateDto partialDto = UserProfileUpdateDto.builder()
                    .userId(1L)
                    .firstName("NewFirstName")
                    .build();

            String originalLastName = sampleUser.getLastName();

            // Act
            UserMapper.applyProfileUpdate(sampleUser, partialDto);

            // Assert
            assertEquals("NewFirstName", sampleUser.getFirstName());
            assertEquals(originalLastName, sampleUser.getLastName());
        }

        @Test
        @DisplayName("Should not update profilePicture when null in DTO")
        void applyProfileUpdate_WithNullProfilePicture_ShouldNotUpdateProfilePicture() {
            // Arrange
            UserProfileUpdateDto partialDto = UserProfileUpdateDto.builder()
                    .userId(1L)
                    .firstName("NewFirstName")
                    .build();

            String originalProfilePicture = sampleUser.getProfilePicture();

            // Act
            UserMapper.applyProfileUpdate(sampleUser, partialDto);

            // Assert
            assertEquals(originalProfilePicture, sampleUser.getProfilePicture());
        }

        @Test
        @DisplayName("Should not modify email (read-only field)")
        void applyProfileUpdate_ShouldNotModifyEmail() {
            // Arrange
            String originalEmail = sampleUser.getEmail();

            // Act
            UserMapper.applyProfileUpdate(sampleUser, sampleProfileUpdateDto);

            // Assert - Email should remain unchanged
            assertEquals(originalEmail, sampleUser.getEmail());
        }

        @Test
        @DisplayName("Should handle all null fields in DTO")
        void applyProfileUpdate_WithAllNullFields_ShouldNotModifyUser() {
            // Arrange
            UserProfileUpdateDto emptyDto = UserProfileUpdateDto.builder()
                    .userId(1L)
                    .build();

            String originalFirstName = sampleUser.getFirstName();
            String originalLastName = sampleUser.getLastName();
            String originalProfilePicture = sampleUser.getProfilePicture();

            // Act
            UserMapper.applyProfileUpdate(sampleUser, emptyDto);

            // Assert - All fields should remain unchanged
            assertEquals(originalFirstName, sampleUser.getFirstName());
            assertEquals(originalLastName, sampleUser.getLastName());
            assertEquals(originalProfilePicture, sampleUser.getProfilePicture());
        }
    }
}
