package com.studyplanner.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.mapper.TaskMapper;
import com.studyplanner.backend.repository.TaskRepository;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.service.ReminderService;
import com.studyplanner.backend.service.TaskService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ReminderService reminderService;
    private final CalendarServiceImpl calendarService;

    // --- Helper ---
    // Load a User
    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not Found "));
    }

    // Load a Task
    private Task findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not Found "));
    }

    // Verify a Task belongs to a User
    private void verifyOwnership(Task task, Long userId) {
        if (!task.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Access Denied");
        }
    }

    // --- CREATE ---
    @Override
    @Transactional
    public TaskDto createTask(TaskDto taskDto) {
        User user = findUser(taskDto.getUserId());
        Task task = TaskMapper.mapToTask(taskDto, user);
        Task saved = taskRepository.save(task);

        // Auto create a reminder for the task
        reminderService.createReminderForTask(saved);
        syncTaskToCalendar(saved);
        return TaskMapper.mapToTaskDto(saved);
    }

    // --- READ ---
    // Get Task by Id
    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(Long taskId, Long userId) {
        Task task = findTask(taskId);
        verifyOwnership(task, userId);
        return TaskMapper.mapToTaskDto(task);
    }

    // Get all tasks for a user
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByUserId(Long userId) {
        findUser(userId);
        return taskRepository.findByUserId(userId).stream().map(TaskMapper::mapToTaskDto).collect(Collectors.toList());
    }

    // Filter by priority
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByPriority(Long userId, Priority priority) {
        findUser(userId); // validate user exists
        return taskRepository.findByUserIdAndPriority(userId, priority)
                .stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    // Filter by status
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByStatus(Long userId, Status status) {
        findUser(userId);
        return taskRepository.findByUserIdAndStatus(userId, status)
                .stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    // Filter by isCompleted
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByCompleted(Long userId, boolean completed) {
        findUser(userId);
        return taskRepository.findByUserIdAndCompleted(userId, completed)
                .stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    // Filter by date range
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByDateRange(Long userId,
                                             LocalDateTime start,
                                             LocalDateTime end) {
        findUser(userId);
        return taskRepository
                .findByUserIdAndTaskDeadlineBetween(userId, start, end)
                .stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    // Filter by deadline
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByDeadline(Long userId, LocalDateTime taskDeadline) {
        findUser(userId);
        return taskRepository.findByUserIdAndTaskDeadline(userId, taskDeadline)
                .stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    // Filter by status and priority
    @Override
    @Transactional(readOnly = true)
    public List<TaskDto> getTasksByStatusAndPriority(Long userId, Status status, Priority priority) {
        findUser(userId);
        return taskRepository.findByUserIdAndStatusAndPriority(userId, status, priority)
                .stream()
                .map(TaskMapper::mapToTaskDto)
                .collect(Collectors.toList());
    }

    // --- UPDATE ---

    @Override
    @Transactional
    public TaskDto updateTask(Long userId, Long taskId, TaskDto taskDto) {
        Task task = findTask(taskId);
        verifyOwnership(task, userId);

        LocalDateTime oldDeadline = task.getTaskDeadline();
        TaskMapper.updateTask(task, taskDto);
        Task updated = taskRepository.save(task);

        // If deadline changed, cancel the old reminder and create a new one
        boolean deadlineChanged = !Objects.equals(oldDeadline, task.getTaskDeadline());
        if (deadlineChanged) {
            reminderService.cancelReminderForTask(taskId);
            reminderService.createReminderForTask(updated);
        }

        syncTaskToCalendar(updated);

        return TaskMapper.mapToTaskDto(updated);
    }

    @Override
    @Transactional
    public TaskDto markTaskAsCompleted(Long taskId, Long userId, boolean completed) {
        Task task = findTask(taskId);
        verifyOwnership(task, userId);
        task.setCompleted(completed);
        if (completed) {
            task.setStatus(Status.COMPLETED);
            // Task is done - no need for a reminder
            reminderService.cancelReminderForTask(taskId);
        }
        return TaskMapper.mapToTaskDto(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskDto updateTaskStatus(Long taskId, Long userId, String status) {
        Task task = findTask(taskId);
        verifyOwnership(task, userId);
        task.setStatus(Status.valueOf(status));
        return TaskMapper.mapToTaskDto(taskRepository.save(task));
    }

    // update task priority
    @Override
    @Transactional
    public TaskDto updateTaskPriority(Long taskId, Long userId, Priority priority) {
        Task task = findTask(taskId);
        verifyOwnership(task, userId);
        task.setPriority(priority);
        return TaskMapper.mapToTaskDto(taskRepository.save(task));
    }

    // --- DELETE ---
    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = findTask(taskId);
        verifyOwnership(task, userId);
        // cancel reminder before deleting task
        reminderService.cancelReminderForTask(taskId);
        taskRepository.delete(task);
    }

    private void syncTaskToCalendar(Task task) {
        try {
            String eventId = calendarService.pushToCalendar(task);
            if (eventId != null) {
                log.info("Task synced to Google Calendar with event ID: {}", eventId);
            }
        } catch (Exception e) {
            log.error("Failed to sync task to Google Calendar: {}", e.getMessage(), e);
        }
    }
}