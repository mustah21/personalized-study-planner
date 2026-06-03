package com.studyplanner.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studyplanner.backend.dto.ShareTaskDto;
import com.studyplanner.backend.dto.TaskShareInviteDto;
import com.studyplanner.backend.dto.UserLoginDto;
import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.TaskShareInvite;
import com.studyplanner.backend.entity.TaskShareInvite.InviteStatus;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.repository.TaskRepository;
import com.studyplanner.backend.repository.TaskShareInviteRepository;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.service.EmailService;
import com.studyplanner.backend.service.ReminderService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ReminderService reminderService;

    @Mock
    private TaskShareInviteRepository inviteRepository;

    private UserServiceImpl userService;

    private User sender;
    private User receiver;
    private Task senderTask;
    private TaskShareInvite pendingInvite;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository, emailService, taskRepository, reminderService, inviteRepository, "http://localhost:8080");

        sender = User.builder()
                .id(1L)
                .firstName("Aashish")
                .lastName("Sender")
                .email("sender@example.com")
                .password("$2a$10$abcdefghijklmnopqrstuv12345678901234567890123456789012")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        receiver = User.builder()
                .id(2L)
                .firstName("Rita")
                .lastName("Receiver")
                .email("receiver@example.com")
                .createdAt(LocalDateTime.now().minusDays(2))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();

        senderTask = Task.builder()
                .id(101L)
                .user(sender)
                .taskName("Math revision")
                .taskDescription("Complete chapter 5")
                .taskDeadline(LocalDateTime.now().plusDays(2))
                .priority(Task.Priority.HIGH)
                .status(Task.Status.PENDING)
                .completed(false)
                .build();

        pendingInvite = TaskShareInvite.builder()
                .id(201L)
                .sender(sender)
                .receiver(receiver)
                .task(senderTask)
                .inviteToken("token-201")
                .status(InviteStatus.PENDING)
                .createdAt(LocalDateTime.now().minusHours(1))
                .build();
    }

    @Nested
    class AuthTests {

        @Test
        void createUser_WithValidData_ShouldPersistAndReturnDto() {
            UserRegisterDto dto = new UserRegisterDto();
            dto.setEmail("nina@example.com");
            dto.setPassword("plainPassword");

            when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User saved = inv.getArgument(0);
                saved.setId(99L);
                return saved;
            });

            UserProfileUpdateDto result = userService.createUser(dto);

            assertNotNull(result);
            assertEquals(99L, result.getUserId());
            assertEquals("nina@example.com", result.getEmail());
            verify(emailService, times(1)).sendWelcomeEmail(eq("nina@example.com"), anyString());
        }

        @Test
        void createUser_WithExistingEmail_ShouldThrow() {
            UserRegisterDto dto = new UserRegisterDto();
            dto.setEmail(sender.getEmail());
            dto.setPassword("abc12345");

            when(userRepository.findByEmail(sender.getEmail())).thenReturn(Optional.of(sender));

            RuntimeException ex = assertThrows(RuntimeException.class, () -> userService.createUser(dto));
            assertEquals("Email already registered", ex.getMessage());
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void login_WithUnknownEmail_ShouldThrow() {
            UserLoginDto dto = new UserLoginDto();
            dto.setEmail("missing@example.com");
            dto.setPassword("secret123");

            when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.login(dto));
            assertEquals("Invalid email or password", ex.getMessage());
        }
    }

    @Nested
    class ShareFlowTests {

        @Test
        void shareTasks_WithSelfRecipient_ShouldThrow() {
            ShareTaskDto request = ShareTaskDto.builder()
                    .senderUserId(1L)
                    .receiverUserIds(List.of(1L))
                    .taskIds(List.of(101L))
                    .build();

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.shareTasks(request));
            assertEquals("You cannot share tasks with yourself", ex.getMessage());
        }

        @Test
        void shareTasks_WithOwnerAndRecipient_ShouldCreateInviteAndSendEmail() {
            ShareTaskDto request = ShareTaskDto.builder()
                    .senderUserId(1L)
                    .receiverUserIds(List.of(2L))
                    .taskIds(List.of(101L))
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(taskRepository.findById(101L)).thenReturn(Optional.of(senderTask));
            when(inviteRepository.existsByReceiverIdAndTaskIdAndStatus(2L, 101L, InviteStatus.PENDING))
                    .thenReturn(false);
            when(inviteRepository.save(any(TaskShareInvite.class))).thenAnswer(inv -> {
                TaskShareInvite saved = inv.getArgument(0);
                saved.setId(777L);
                saved.setCreatedAt(LocalDateTime.now());
                return saved;
            });

            List<TaskShareInviteDto> response = userService.shareTasks(request);

            assertEquals(1, response.size());
            assertEquals(InviteStatus.PENDING, response.get(0).getStatus());
            verify(emailService, times(1)).sendShareInviteEmail(
                    eq("receiver@example.com"),
                    eq("Rita"),
                    eq(sender),
                    eq(senderTask),
                    anyString(),
                    anyString());
        }

        @Test
        void shareTasks_WhenTaskNotOwnedBySender_ShouldThrowUnauthorized() {
            User anotherOwner = User.builder().id(55L).email("other@example.com").build();
            Task foreignTask = Task.builder().id(333L).user(anotherOwner).taskName("Foreign").build();

            ShareTaskDto request = ShareTaskDto.builder()
                    .senderUserId(1L)
                    .receiverUserIds(List.of(2L))
                    .taskIds(List.of(333L))
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
            when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
            when(taskRepository.findById(333L)).thenReturn(Optional.of(foreignTask));

            assertThrows(UnauthorizedAccessException.class, () -> userService.shareTasks(request));
            verify(inviteRepository, never()).save(any(TaskShareInvite.class));
        }
    }

    @Nested
    class InviteDecisionTests {

        @Test
        void acceptInvite_WithPendingInvite_ShouldCopyTaskAndMarkAccepted() {
            when(inviteRepository.findByTokenWithDetails("token-201")).thenReturn(Optional.of(pendingInvite));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
                Task copied = inv.getArgument(0);
                copied.setId(500L);
                return copied;
            });
            when(inviteRepository.save(any(TaskShareInvite.class))).thenAnswer(inv -> inv.getArgument(0));

            TaskShareInviteDto result = userService.acceptInvite("token-201");

            assertEquals(InviteStatus.ACCEPTED, result.getStatus());
            ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
            verify(taskRepository).save(taskCaptor.capture());
            assertEquals("sender@example.com", taskCaptor.getValue().getSharedByEmail());
            assertEquals(Task.Status.PENDING, taskCaptor.getValue().getStatus());
            assertFalse(taskCaptor.getValue().isCompleted());
            verify(reminderService, times(1)).createReminderForTask(any(Task.class));
        }

        @Test
        void acceptInvite_WithAlreadyAcceptedInvite_ShouldThrow() {
            pendingInvite.setStatus(InviteStatus.ACCEPTED);
            when(inviteRepository.findByTokenWithDetails("token-201")).thenReturn(Optional.of(pendingInvite));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.acceptInvite("token-201"));
            assertTrue(ex.getMessage().contains("already been accepted"));
            verify(taskRepository, never()).save(any(Task.class));
        }

        @Test
        void declineInvite_WithPendingInvite_ShouldMarkDeclined() {
            when(inviteRepository.findByTokenWithDetails("token-201")).thenReturn(Optional.of(pendingInvite));
            when(inviteRepository.save(any(TaskShareInvite.class))).thenAnswer(inv -> inv.getArgument(0));

            TaskShareInviteDto result = userService.declineInvite("token-201");

            assertEquals(InviteStatus.DECLINED, result.getStatus());
            verify(inviteRepository, times(1)).save(pendingInvite);
        }

        @Test
        void declineInvite_WithMissingToken_ShouldThrowNotFound() {
            when(inviteRepository.findByTokenWithDetails(anyString())).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> userService.declineInvite("missing"));
            assertEquals("Invite not found", ex.getMessage());
        }
    }

    @Test
    void getPendingAndCount_ShouldUseInviteRepository() {
        when(inviteRepository.findByReceiverIdAndStatusWithDetails(2L, InviteStatus.PENDING))
                .thenReturn(List.of(pendingInvite));
        when(inviteRepository.countByReceiverIdAndStatus(2L, InviteStatus.PENDING))
                .thenReturn(1L);

        List<TaskShareInviteDto> pending = userService.getPendingInvites(2L);
        long count = userService.countPendingInvites(2L);

        assertEquals(1, pending.size());
        assertEquals(1L, count);
        verify(inviteRepository, times(1)).findByReceiverIdAndStatusWithDetails(2L, InviteStatus.PENDING);
        verify(inviteRepository, times(1)).countByReceiverIdAndStatus(2L, InviteStatus.PENDING);
    }

    @Test
    void searchUsers_BlankQuery_ShouldReturnEmptyWithoutRepoCall() {
        List<?> result = userService.searchUsers("   ", 1L);
        assertTrue(result.isEmpty());
        verify(userRepository, never()).searchUsers(anyString(), anyLong());
    }

    @Test
    void profileAndSearchAndInviteListing_ShouldCoverAdditionalPaths() {
        UserProfileUpdateDto update = UserProfileUpdateDto.builder()
                .firstName("Updated")
                .lastName("Name")
                .profilePicture("pic.png")
                .build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        UserProfileUpdateDto updated = userService.updateProfile(1L, update);
        assertEquals("Updated", updated.getFirstName());

        when(userRepository.searchUsers("ri", 1L)).thenReturn(List.of(receiver));
        assertEquals(1, userService.searchUsers("ri", 1L).size());
        assertEquals("Rita", userService.searchUsers("ri", 1L).get(0).getFirstName());

        when(inviteRepository.findByReceiverIdWithDetails(2L)).thenReturn(List.of(pendingInvite));
        assertEquals(1, userService.getAllReceivedInvites(2L).size());
    }

    @Test
    void loginWithWrongPassword_AndGetMissingUser_ShouldThrow() {
        UserLoginDto dto = new UserLoginDto();
        dto.setEmail("sender@example.com");
        dto.setPassword("wrong-password");
        when(userRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(sender));
        assertThrows(IllegalArgumentException.class, () -> userService.login(dto));

        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(999L));
    }
}
