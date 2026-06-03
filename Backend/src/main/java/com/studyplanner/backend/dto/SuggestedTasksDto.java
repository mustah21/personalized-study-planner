package com.studyplanner.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studyplanner.backend.entity.SuggestedLLM.Priority;
import com.studyplanner.backend.entity.SuggestedLLM.Status;
import com.studyplanner.backend.entity.SuggestedLLM.SuggestedStatus;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuggestedTasksDto {

    private Long taskId;
    private Long userId;
    private String taskName;
    private String taskDescription;
    private LocalDateTime taskDeadline;
    private Priority priority;
    private Status status;
    private SuggestedStatus suggestedStatus;
    private String llmModel;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // if accepted, the ID of the task created in the database
    private Long acceptedTaskId;
}