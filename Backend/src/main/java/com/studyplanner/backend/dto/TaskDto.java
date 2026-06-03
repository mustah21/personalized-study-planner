package com.studyplanner.backend.dto;

import java.time.LocalDateTime;

import com.studyplanner.backend.entity.Task;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TaskDto {

    private Long taskId;
    // userId is set by the controller from the JWT token, not from client request
    private Long userId;
    private String taskName;
    private String taskDescription;
    private LocalDateTime taskDeadline;
    private Task.Priority priority;
    private Task.Status status;
    private boolean completed;
    private String sharedByEmail;
    private Task.Language language;

}
