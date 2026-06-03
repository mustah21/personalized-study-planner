package com.studyplanner.backend.service;

import com.studyplanner.backend.dto.LlmTaskGenerationRequest;
import com.studyplanner.backend.dto.LlmTaskGenerationResponse;

public interface LlmService {

    // generate task suggestion using llm based on user prompt.

    LlmTaskGenerationResponse generateTaskSuggestions(LlmTaskGenerationRequest request, Long userId);

    // check if user has exceeded their LLM limit for the period of time.
    // user can have 50 suggestion per month
    boolean canUserRequestMoreSuggestions(Long userId);

    // return the number of suggestions left for the user
    int getRemainingMonthlyQuota(Long userId);
    LlmTaskGenerationResponse chat(LlmTaskGenerationRequest prompt);

}