import { describe, it, expect, beforeEach } from 'vitest';
import { loginWithEmail, getCurrentUser, logout } from './auth';

/**
 * Unit tests for auth service.
 * Tests cover user authentication, session management, and logout functionality.
 */
describe('Auth Service', () => {
  beforeEach(() => {
    // Clear user state before each test
    logout();
  });

  describe('loginWithEmail', () => {
    it('should return a user object with correct email', async () => {
      // Arrange
      const email = 'test@example.com';

      // Act
      const user = await loginWithEmail(email);

      // Assert
      expect(user).toBeDefined();
      expect(user.email).toBe(email);
    });

    it('should generate username from email prefix', async () => {
      // Arrange
      const email = 'johndoe@example.com';

      // Act
      const user = await loginWithEmail(email);

      // Assert
      expect(user.username).toBe('johndoe');
    });

    it('should set default username when email has no prefix', async () => {
      // Arrange
      const email = '@example.com';

      // Act
      const user = await loginWithEmail(email);

      // Assert
      expect(user.username).toBe('user');
    });

    it('should return user with id', async () => {
      // Arrange
      const email = 'test@example.com';

      // Act
      const user = await loginWithEmail(email);

      // Assert
      expect(user.id).toBeDefined();
      expect(user.id).toBe('1');
    });

    it('should return user with empty name and bio', async () => {
      // Arrange
      const email = 'test@example.com';

      // Act
      const user = await loginWithEmail(email);

      // Assert
      expect(user.name).toBe('');
      expect(user.bio).toBe('');
    });

    it('should set the current user after login', async () => {
      // Arrange
      const email = 'test@example.com';

      // Act
      await loginWithEmail(email);
      const currentUser = getCurrentUser();

      // Assert
      expect(currentUser).not.toBeNull();
      expect(currentUser?.email).toBe(email);
    });
  });

  describe('getCurrentUser', () => {
    it('should return null when no user is logged in', () => {
      // Act
      const user = getCurrentUser();

      // Assert
      expect(user).toBeNull();
    });

    it('should return the logged-in user', async () => {
      // Arrange
      const email = 'test@example.com';
      await loginWithEmail(email);

      // Act
      const user = getCurrentUser();

      // Assert
      expect(user).not.toBeNull();
      expect(user?.email).toBe(email);
    });

    it('should return the same user object reference', async () => {
      // Arrange
      const email = 'test@example.com';
      const loggedInUser = await loginWithEmail(email);

      // Act
      const currentUser = getCurrentUser();

      // Assert
      expect(currentUser).toBe(loggedInUser);
    });
  });

  describe('logout', () => {
    it('should clear the current user', async () => {
      // Arrange
      await loginWithEmail('test@example.com');
      expect(getCurrentUser()).not.toBeNull();

      // Act
      logout();

      // Assert
      expect(getCurrentUser()).toBeNull();
    });

    it('should be safe to call when no user is logged in', () => {
      // Act & Assert - Should not throw
      expect(() => logout()).not.toThrow();
      expect(getCurrentUser()).toBeNull();
    });

    it('should allow re-login after logout', async () => {
      // Arrange
      await loginWithEmail('first@example.com');
      logout();

      // Act
      const user = await loginWithEmail('second@example.com');

      // Assert
      expect(user.email).toBe('second@example.com');
      expect(getCurrentUser()?.email).toBe('second@example.com');
    });
  });
});

