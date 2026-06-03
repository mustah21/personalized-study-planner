package com.studyplanner.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionBatchResponseDto {

    private List<TaskDto> acceptedTasks; // Tasks that were accepted and copied to main table
    private List<SuggestedTasksDto> declined; // Suggestions that were declined
    private int totalProcessed;
    private int acceptedCount;
    private int declinedCount;
}