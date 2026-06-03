package com.studyplanner.backend.service;

import java.util.List;
import java.util.Map;

import com.studyplanner.backend.dto.SuggestedTasksDto;
import com.studyplanner.backend.dto.SuggestionBatchResponseDto;
import com.studyplanner.backend.dto.SuggestionResponseDto;
import com.studyplanner.backend.dto.TaskDto;

public interface SuggestedTaskService {

    // get all pending suggestions for a user
    List<SuggestedTasksDto> getPendingSuggestions(Long userId);

    // get all suggestions for a user regardless of status, for analytics
    List<SuggestedTasksDto> getAllSuggestions(Long userId);

    // accept a suggested task and copies it to the task table and also create a
    // reminder for it if deadline qualifies.
    TaskDto acceptSuggestion(Long suggestionId, Long userId);

    // reject a suggested task
    SuggestedTasksDto declineSuggestion(Long suggestionId, Long userId);

    // accept multiple suggestion at once.
    SuggestionBatchResponseDto respondToSuggestions(SuggestionResponseDto response, Long userId);

    // accept multiple suggestion at once.
    List<TaskDto> acceptMultipleSuggestions(List<Long> suggestionIds, Long userId);

    // ----------------- this is analytical part --------------------
    // ----------------- Currently not in use (created for future use
    // cases)------------------
    /**
     * Get analytics for a user:
     * - Total suggestions generated
     * - Accepted count
     * - Declined count
     * - Pending count
     * - Acceptance rate (%)
     * - Remaining monthly quota
     *
     * @param userId User to analyze
     * @return Map with analytics data
     */

    Map<String, Object> getUserAnalytics(Long userId);
}