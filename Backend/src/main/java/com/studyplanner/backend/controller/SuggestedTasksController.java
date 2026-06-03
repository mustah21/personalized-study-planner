package com.studyplanner.backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.studyplanner.backend.dto.ApiResponse;
import com.studyplanner.backend.dto.LlmTaskGenerationRequest;
import com.studyplanner.backend.dto.LlmTaskGenerationResponse;
import com.studyplanner.backend.dto.SuggestedTasksDto;
import com.studyplanner.backend.dto.SuggestionBatchResponseDto;
import com.studyplanner.backend.dto.SuggestionResponseDto;
import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.service.LlmService;
import com.studyplanner.backend.service.SuggestedTaskService;
import com.studyplanner.backend.util.SecurityUtils;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("api/v1/suggestions")
@AllArgsConstructor
public class SuggestedTasksController {

    private final LlmService llmService;
    private final SuggestedTaskService suggestedTaskService;
    private final SecurityUtils securityUtils;

    // Generate Post /api/v1/suggestions/generate

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<LlmTaskGenerationResponse>> generateSuggestions(
            @Valid @RequestBody LlmTaskGenerationRequest request) {

        Long userId = securityUtils.getAuthenticatedUserId();
        LlmTaskGenerationResponse response = llmService.generateTaskSuggestions(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<LlmTaskGenerationResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message(response.getMessage())
                .data(response)
                .build());
    }

    // RESPOND (BATCH) POST /api/v1/suggestions/respond
    // Single endpoint for accept and/or decline — frontend sends both lists

    @PostMapping("/respond")
    public ResponseEntity<ApiResponse<SuggestionBatchResponseDto>> respondToSuggestions(
            @Valid @RequestBody SuggestionResponseDto response) {

        Long userId = securityUtils.getAuthenticatedUserId();
        SuggestionBatchResponseDto result = suggestedTaskService.respondToSuggestions(response, userId);

        return ResponseEntity.ok(
                ApiResponse.<SuggestionBatchResponseDto>builder()
                        .status(HttpStatus.OK.value())
                        .message(String.format("Processed %d suggestion(s): %d accepted, %d declined",
                                result.getTotalProcessed(),
                                result.getAcceptedCount(),
                                result.getDeclinedCount()))
                        .data(result)
                        .build());
    }

    // get pending suggestion
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<SuggestedTasksDto>>> getPending() {
        Long userId = securityUtils.getAuthenticatedUserId();
        List<SuggestedTasksDto> pending = suggestedTaskService.getPendingSuggestions(userId);
        return ResponseEntity.ok(ApiResponse.<List<SuggestedTasksDto>>builder()
                .status(HttpStatus.OK.value())
                .message(pending.size() + " pending suggestion(s)")
                .data(pending)
                .build());

    }

    // Get All Suggestions
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<SuggestedTasksDto>>> getAll() {
        Long userId = securityUtils.getAuthenticatedUserId();
        List<SuggestedTasksDto> all = suggestedTaskService.getAllSuggestions(userId);
        return ResponseEntity.ok(ApiResponse.<List<SuggestedTasksDto>>builder()
                .status(HttpStatus.OK.value())
                .message(all.size() + " suggestion(s) found")
                .data(all)
                .build());
    }

    // ----------------------- FOR SINGLE USE CASES FRONTEND -----------------------
    // Accept POST
    @PostMapping("/{suggestionId}/accept")
    public ResponseEntity<ApiResponse<TaskDto>> acceptSuggestion(
            @PathVariable Long suggestionId) {
        Long userId = securityUtils.getAuthenticatedUserId();
        TaskDto task = suggestedTaskService.acceptSuggestion(suggestionId, userId);
        return ResponseEntity.ok(ApiResponse.<TaskDto>builder()
                .status(HttpStatus.OK.value())
                .message("Suggestion accepted successfully")
                .data(task)
                .build());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DECLINE POST /api/v1/suggestions/{suggestionId}/decline
    // ─────────────────────────────────────────────────────────────────────────────
    @PostMapping("/{suggestionId}/decline")
    public ResponseEntity<ApiResponse<SuggestedTasksDto>> declineSuggestion(
            @PathVariable Long suggestionId) {

        Long userId = securityUtils.getAuthenticatedUserId();
        SuggestedTasksDto declined = suggestedTaskService.declineSuggestion(suggestionId, userId);

        return ResponseEntity.ok(
                ApiResponse.<SuggestedTasksDto>builder()
                        .status(HttpStatus.OK.value())
                        .message("Suggestion declined")
                        .data(declined)
                        .build());
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // BATCH ACCEPT POST /api/v1/suggestions/accept-multiple
    // ─────────────────────────────────────────────────────────────────────────────

    @PostMapping("/accept-multiple")
    public ResponseEntity<ApiResponse<List<TaskDto>>> acceptMultiple(
            @RequestBody List<Long> suggestionIds) {

        Long userId = securityUtils.getAuthenticatedUserId();
        List<TaskDto> tasks = suggestedTaskService.acceptMultipleSuggestions(suggestionIds, userId);

        return ResponseEntity.ok(
                ApiResponse.<List<TaskDto>>builder()
                        .status(HttpStatus.OK.value())
                        .message(tasks.size() + " suggestion(s) accepted")
                        .data(tasks)
                        .build());
    }

    // ANALYTICS GET /api/v1/suggestions/analytics

    @GetMapping("/analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAnalytics() {
        Long userId = securityUtils.getAuthenticatedUserId();
        Map<String, Object> analytics = suggestedTaskService.getUserAnalytics(userId);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Analytics retrieved")
                        .data(analytics)
                        .build());
    }

    // QUOTA CHECK GET /api/v1/suggestions/quota

    @GetMapping("/quota")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getQuota() {
        Long userId = securityUtils.getAuthenticatedUserId();
        int remaining = llmService.getRemainingMonthlyQuota(userId);
        boolean canRequest = llmService.canUserRequestMoreSuggestions(userId);

        Map<String, Object> quota = Map.of(
                "remainingThisMonth", remaining,
                "canRequestMore", canRequest);

        return ResponseEntity.ok(
                ApiResponse.<Map<String, Object>>builder()
                        .status(HttpStatus.OK.value())
                        .message("Quota information")
                        .data(quota)
                        .build());
    }

}