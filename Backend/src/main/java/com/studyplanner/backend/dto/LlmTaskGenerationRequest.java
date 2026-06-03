package com.studyplanner.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LlmTaskGenerationRequest {

    @NotBlank(message = "Prompt is required")
    private String prompt;
    // optional
    private String additionalContext;
    private boolean sent;
    private String language;

}