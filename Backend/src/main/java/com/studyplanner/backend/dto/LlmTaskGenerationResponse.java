package com.studyplanner.backend.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmTaskGenerationResponse {

    private List<SuggestedTasksDto> suggestions;
    private int totalGenerated;
    private String message;

}