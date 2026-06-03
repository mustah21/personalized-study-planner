package com.studyplanner.backend.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studyplanner.backend.dto.ApiResponse;
import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;
import com.studyplanner.backend.service.TaskService;
import com.studyplanner.backend.util.SecurityUtils;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {

    private final TaskService taskService;
    private final SecurityUtils securityUtils;

    @Autowired
    public TaskController(TaskService taskService, SecurityUtils securityUtils) {
        this.taskService = taskService;
        this.securityUtils = securityUtils;
    }

    // CREAT POST api
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TaskDto>> createTask(@Valid @RequestBody TaskDto taskDto) {
        taskDto.setUserId(securityUtils.getAuthenticatedUserId());
        TaskDto created = taskService.createTask(taskDto);

        ApiResponse<TaskDto> response = ApiResponse.<TaskDto>builder()
                .status(HttpStatus.CREATED.value())
                .message("Task created successfully")
                .data(created)
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // READ GET api

    @GetMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskDto>> getTaskById(@PathVariable Long taskId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        TaskDto task = taskService.getTaskById(taskId, userId);
        return ResponseEntity.ok(ApiResponse.<TaskDto>builder().status(HttpStatus.OK.value())
                .message("Task retrieved successfully").data(task).build());
    }

    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<TaskDto>>> getAllTasksByUser() {

        Long userId = securityUtils.getAuthenticatedUserId();
        List<TaskDto> tasks = taskService.getTasksByUserId(userId);

        return ResponseEntity.ok(
                ApiResponse.<List<TaskDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Tasks retrieved successfully")
                        .data(tasks)
                        .build());
    }

    @GetMapping("/user/filter")
    public ResponseEntity<ApiResponse<List<TaskDto>>> filterTasks(
            @RequestParam(required = false) Priority priority,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime taskDeadline) {

        Long userId = securityUtils.getAuthenticatedUserId();
        List<TaskDto> tasks;

        if (priority != null && status != null) {
            tasks = taskService.getTasksByStatusAndPriority(userId, status, priority);
        } else if (priority != null) {
            tasks = taskService.getTasksByPriority(userId, priority);
        } else if (taskDeadline != null) {
            tasks = taskService.getTasksByDeadline(userId, taskDeadline);
        } else if (status != null) {
            tasks = taskService.getTasksByStatus(userId, status);
        } else if (completed != null) {
            tasks = taskService.getTasksByCompleted(userId, completed);
        } else if (from != null && to != null) {
            tasks = taskService.getTasksByDateRange(userId, from, to);
        } else {
            tasks = taskService.getTasksByUserId(userId);
        }

        return ResponseEntity.ok(
                ApiResponse.<List<TaskDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Tasks filtered successfully")
                        .data(tasks)
                        .build());
    }

    // ---- UPDATE PUT api----

    @PutMapping("/update/{taskId}")
    public ResponseEntity<ApiResponse<TaskDto>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskDto taskDto) {

        Long userId = securityUtils.getAuthenticatedUserId();
        TaskDto updated = taskService.updateTask(userId, taskId, taskDto);
        return ResponseEntity.ok(
                ApiResponse.<TaskDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Task updated successfully")
                        .data(updated)
                        .build());
    }

    @PatchMapping("/update/{taskId}/complete")
    public ResponseEntity<ApiResponse<TaskDto>> markCompleted(
            @PathVariable Long taskId,
            @RequestParam boolean completed) {

        Long userId = securityUtils.getAuthenticatedUserId();
        TaskDto updated = taskService.markTaskAsCompleted(taskId, userId, completed);
        return ResponseEntity.ok(
                ApiResponse.<TaskDto>builder()
                        .status(HttpStatus.OK.value())
                        .message(completed ? "Task marked as completed" : "Task marked as incomplete")
                        .data(updated)
                        .build());
    }

    @PatchMapping("/update/{taskId}/status")
    public ResponseEntity<ApiResponse<TaskDto>> updateStatus(
            @PathVariable Long taskId,
            @RequestParam Status status) {

        Long userId = securityUtils.getAuthenticatedUserId();
        TaskDto updated = taskService.updateTaskStatus(taskId, userId, status.name());
        return ResponseEntity.ok(
                ApiResponse.<TaskDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Task status updated to " + status)
                        .data(updated)
                        .build());
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        taskService.deleteTask(taskId, userId);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .status(HttpStatus.OK.value())
                        .message("Task deleted successfully")
                        .data(null)
                        .build());
    }
}
