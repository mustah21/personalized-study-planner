package com.studyplanner.backend.mapper;

import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;

public class TaskMapper {
    private TaskMapper() {
        /* This utility class should not be instantiated */
    }


    public static TaskDto mapToTaskDto(Task task) {
        return TaskDto.builder()
                .taskId(task.getId())
                .userId(task.getUser().getId())
                .taskName(task.getTaskName())
                .taskDescription(task.getTaskDescription())
                .taskDeadline(task.getTaskDeadline())
                .priority(task.getPriority())
                .status(task.getStatus())
                .completed(task.isCompleted())
                .sharedByEmail(task.getSharedByEmail())
                .language(task.getLanguage())
                .build();
    }

    public static Task mapToTask(TaskDto taskDto, User user) {
        return Task.builder()
                .id(taskDto.getTaskId())
                .user(user)
                .taskName(taskDto.getTaskName())
                .taskDescription(taskDto.getTaskDescription())
                .taskDeadline(taskDto.getTaskDeadline())
                .priority(taskDto.getPriority())
                .status(taskDto.getStatus())
                .completed(taskDto.isCompleted())
                .language(taskDto.getLanguage())
                .build();
    }

    // Update Task from TaskDto to Task entity
    // set method is used rather then builder to update only changed fields
    public static void updateTask(Task task, TaskDto taskDto) {
        task.setTaskName(taskDto.getTaskName());
        task.setTaskDescription(taskDto.getTaskDescription());
        task.setTaskDeadline(taskDto.getTaskDeadline());
        task.setPriority(taskDto.getPriority());
        task.setStatus(taskDto.getStatus());
        task.setCompleted(taskDto.isCompleted());
        task.setLanguage(taskDto.getLanguage());
    }

}
