package com.studyplanner.backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyplanner.backend.dto.ShareTaskDto;
import com.studyplanner.backend.dto.TaskShareInviteDto;
import com.studyplanner.backend.dto.UserLoginDto;
import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.dto.UserSearchdto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.TaskShareInvite;
import com.studyplanner.backend.entity.TaskShareInvite.InviteStatus;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.mapper.TaskShareInviteMapper;
import com.studyplanner.backend.mapper.UserMapper;
import com.studyplanner.backend.repository.TaskRepository;
import com.studyplanner.backend.repository.TaskShareInviteRepository;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.service.EmailService;
import com.studyplanner.backend.service.ReminderService;
import com.studyplanner.backend.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
// No @AllArgsConstructor — single explicit constructor needed for @Value
// injection
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TaskRepository taskRepository;
    private final ReminderService reminderService;
    private final TaskShareInviteRepository inviteRepository;
    private final String backendBaseUrl;

    public UserServiceImpl(UserRepository userRepository,
            EmailService emailService,
            TaskRepository taskRepository,
            ReminderService reminderService,
            TaskShareInviteRepository inviteRepository,
            @Value("${app.backend.base-url}") String backendBaseUrl) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.taskRepository = taskRepository;
        this.reminderService = reminderService;
        this.inviteRepository = inviteRepository;
        this.backendBaseUrl = backendBaseUrl;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // AUTH
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public UserProfileUpdateDto createUser(UserRegisterDto userDto) {
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new UnauthorizedAccessException("Email already registered");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        User user = UserMapper.mapToUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User saved = userRepository.save(user);

        // Welcome email failure must not break registration
        try {
            String firstName = (saved.getFirstName() != null && !saved.getFirstName().isBlank())
                    ? saved.getFirstName()
                    : "there";
            emailService.sendWelcomeEmail(saved.getEmail(), firstName);
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", saved.getEmail(), e.getMessage());
        }

        return UserMapper.mapToUserDto(saved);
    }

    @Override
    public UserProfileUpdateDto login(UserLoginDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(userDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return UserMapper.mapToUserDto(user);
    }

    @Override
    public UserProfileUpdateDto updateProfile(Long userId, UserProfileUpdateDto userDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot update: User not found"));

        UserMapper.applyProfileUpdate(user, userDto);
        user.setUpdatedAt(LocalDateTime.now());
        return UserMapper.mapToUserDto(userRepository.save(user));
    }

    @Override
    public UserProfileUpdateDto getUserById(Long userId) {
        return UserMapper.mapToUserDto(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SEARCH
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<UserSearchdto> searchUsers(String query, Long excludeUserId) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return userRepository.searchUsers(query.trim(), excludeUserId)
                .stream()
                .map(this::mapToUserSearchDto)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // SHARE — creates invites + sends emails, does NOT copy tasks yet
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public List<TaskShareInviteDto> shareTasks(ShareTaskDto shareTaskDto) {

        if (shareTaskDto.getReceiverUserIds().contains(shareTaskDto.getSenderUserId())) {
            throw new IllegalArgumentException("You cannot share tasks with yourself");
        }

        User sender = userRepository.findById(shareTaskDto.getSenderUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        List<User> recipients = shareTaskDto.getReceiverUserIds().stream()
                .map(id -> userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Recipient not found with id: " + id)))
                .toList();

        List<Task> tasks = shareTaskDto.getTaskIds().stream()
                .map(id -> {
                    Task task = taskRepository.findById(id)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Task not found with id: " + id));
                    if (!task.getUser().getId().equals(sender.getId())) {
                        throw new UnauthorizedAccessException(
                                "You can only share your own tasks. Task id: " + id);
                    }
                    return task;
                })
                .toList();

        List<TaskShareInviteDto> createdInvites = new ArrayList<>();

        for (User recipient : recipients) {
            for (Task task : tasks) {

                if (inviteRepository.existsByReceiverIdAndTaskIdAndStatus(
                        recipient.getId(), task.getId(), InviteStatus.PENDING)) {
                    log.info("Skipping duplicate invite: task {} → recipient {}",
                            task.getId(), recipient.getId());
                    continue;
                }

                String inviteToken = UUID.randomUUID().toString();

                TaskShareInvite invite = TaskShareInvite.builder()
                        .sender(sender)
                        .receiver(recipient)
                        .task(task)
                        .status(InviteStatus.PENDING)
                        .inviteToken(inviteToken)
                        .build();

                TaskShareInvite saved = inviteRepository.save(invite);

                String acceptUrl = backendBaseUrl + "/api/v1/users/invites/accept?token=" + inviteToken;
                String declineUrl = backendBaseUrl + "/api/v1/users/invites/decline?token=" + inviteToken;

                emailService.sendShareInviteEmail(
                        recipient.getEmail(),
                        recipient.getFirstName(),
                        sender,
                        task,
                        acceptUrl,
                        declineUrl);

                createdInvites.add(TaskShareInviteMapper.mapToDto(saved));
                log.info("Invite created: task '{}' from user {} to user {}",
                        task.getTaskName(), sender.getId(), recipient.getId());
            }
        }

        return createdInvites;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ACCEPT — copies task to recipient's calendar
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TaskShareInviteDto acceptInvite(String inviteToken) {
        TaskShareInvite invite = inviteRepository.findByTokenWithDetails(inviteToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This invite has already been " + invite.getStatus().name().toLowerCase());
        }

        Task original = invite.getTask();
        User recipient = invite.getReceiver();

        Task copiedTask = Task.builder()
                .user(recipient)
                .taskName(original.getTaskName())
                .taskDescription(original.getTaskDescription())
                .taskDeadline(original.getTaskDeadline())
                .priority(original.getPriority())
                .status(Task.Status.PENDING)
                .completed(false)
                .sharedByEmail(invite.getSender().getEmail())
                .build();

        Task savedTask = taskRepository.save(copiedTask);
        reminderService.createReminderForTask(savedTask);

        invite.setStatus(InviteStatus.ACCEPTED);
        invite.setRespondedAt(LocalDateTime.now());
        invite.setSharedTask(savedTask);
        inviteRepository.save(invite);

        log.info("Invite accepted: task '{}' added to user {} calendar",
                original.getTaskName(), recipient.getId());

        return TaskShareInviteMapper.mapToDto(invite);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DECLINE — marks as declined, no task copy
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TaskShareInviteDto declineInvite(String inviteToken) {
        TaskShareInvite invite = inviteRepository.findByTokenWithDetails(inviteToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invite not found"));

        if (invite.getStatus() != InviteStatus.PENDING) {
            throw new IllegalArgumentException(
                    "This invite has already been " + invite.getStatus().name().toLowerCase());
        }

        invite.setStatus(InviteStatus.DECLINED);
        invite.setRespondedAt(LocalDateTime.now());
        inviteRepository.save(invite);

        log.info("Invite declined: task '{}' by user {}",
                invite.getTask().getTaskName(), invite.getReceiver().getId());

        return TaskShareInviteMapper.mapToDto(invite);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // NOTIFICATIONS
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<TaskShareInviteDto> getPendingInvites(Long userId) {
        return inviteRepository.findByReceiverIdAndStatusWithDetails(userId, InviteStatus.PENDING)
                .stream()
                .map(TaskShareInviteMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskShareInviteDto> getAllReceivedInvites(Long userId) {
        return inviteRepository.findByReceiverIdWithDetails(userId)
                .stream()
                .map(TaskShareInviteMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countPendingInvites(Long userId) {
        return inviteRepository.countByReceiverIdAndStatus(userId, InviteStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Private mapper helper
    // ─────────────────────────────────────────────────────────────────────────────

    private UserSearchdto mapToUserSearchDto(User user) {
        return UserSearchdto.builder()
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }
}