package com.studyplanner.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;

public interface TaskService {

    // create new task for user
    TaskDto createTask(TaskDto taskDto);

    // -----READ-------
    // get single task by id
    TaskDto getTaskById(Long taskId, Long userId);

    // get all tasks belonging to user
    List<TaskDto> getTasksByUserId(Long userId);

    // Filter by priority
    List<TaskDto> getTasksByPriority(Long userId, Priority priority);

    // Filter By status
    List<TaskDto> getTasksByStatus(Long userId, Status status);

    // Filter By completed status
    List<TaskDto> getTasksByCompleted(Long userId, boolean completed);

    // by date range
    List<TaskDto> getTasksByDateRange(Long userId, LocalDateTime start, LocalDateTime end);

    // by deadline
    List<TaskDto> getTasksByDeadline(Long userId, LocalDateTime taskDeadline);

    // Filter by status and priority for a user
    List<TaskDto> getTasksByStatusAndPriority(Long userId, Status status, Priority priority);

    // -----UPDATE-------
    // update task by id
    TaskDto updateTask(Long userId, Long taskId, TaskDto taskDto);

    // Mark task as completed
    TaskDto markTaskAsCompleted(Long taskId, Long userId, boolean completed);

    // Update task status
    TaskDto updateTaskStatus(Long taskId, Long userId, String status);

    // Update task priority
    TaskDto updateTaskPriority(Long taskId, Long userId, Priority priority);

    // -----DELETE-------
    // delete task by id
    void deleteTask(Long taskId, Long userId);
}
