package com.studyplanner.backend.mapper;

import com.studyplanner.backend.dto.SuggestedTasksDto;
import com.studyplanner.backend.entity.SuggestedLLM;

public class SuggestedTasksMapper {
    private SuggestedTasksMapper() {
        /* This utility class should not be instantiated */
    }


    public static SuggestedTasksDto toDto(SuggestedLLM entity) {
        return SuggestedTasksDto.builder()
                .taskId(entity.getId())
                .userId(entity.getUser().getId())
                .taskName(entity.getTaskName())
                .taskDescription(entity.getTaskDescription())
                .taskDeadline(entity.getTaskDeadline())
                .priority(entity.getPriority())
                .status(entity.getStatus())
                .suggestedStatus(entity.getSuggestedStatus())
                .llmModel(entity.getLlmModel())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .acceptedTaskId(entity.getAcceptedTask() != null
                        ? entity.getAcceptedTask().getId()
                        : null)
                .build();
    }

    // used when creating new suggestion from llms
    public static SuggestedLLM toEntity(SuggestedTasksDto dto) {
        return SuggestedLLM.builder()
                .taskName(dto.getTaskName())
                .taskDeadline(dto.getTaskDeadline())
                .taskDescription(dto.getTaskDescription())
                .priority(dto.getPriority())
                .status(dto.getStatus())
                .suggestedStatus(dto.getSuggestedStatus())
                .llmModel(dto.getLlmModel())
                .build();
    }
}