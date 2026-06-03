package com.studyplanner.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplanner.backend.dto.LlmTaskGenerationRequest;
import com.studyplanner.backend.dto.LlmTaskGenerationResponse;
import com.studyplanner.backend.dto.SuggestedTasksDto;
import com.studyplanner.backend.dto.SuggestionBatchResponseDto;
import com.studyplanner.backend.dto.SuggestionResponseDto;
import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.SuggestedLLM.Priority;
import com.studyplanner.backend.entity.SuggestedLLM.SuggestedStatus;
import com.studyplanner.backend.service.LlmService;
import com.studyplanner.backend.service.SuggestedTaskService;
import com.studyplanner.backend.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuggestedTasksController Tests")
class SuggestedTasksControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private LlmService llmService;

    @Mock
    private SuggestedTaskService suggestedTaskService;

    private TestSecurityUtils testSecurityUtils;

    @BeforeEach
    void setUp() {
        testSecurityUtils = new TestSecurityUtils();
        SuggestedTasksController controller = new SuggestedTasksController(llmService, suggestedTaskService, testSecurityUtils);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    @Test
    void generateSuggestions_ShouldReturnCreated() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(1L);
        LlmTaskGenerationRequest req = LlmTaskGenerationRequest.builder().prompt("make tasks").build();
        LlmTaskGenerationResponse resp = LlmTaskGenerationResponse.builder()
                .message("Generated")
                .totalGenerated(1)
                .suggestions(List.of())
                .build();
        when(llmService.generateTaskSuggestions(any(LlmTaskGenerationRequest.class), eq(1L))).thenReturn(resp);

        mockMvc.perform(post("/api/v1/suggestions/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("Generated"));
    }

    @Test
    void respondToSuggestions_ShouldReturnSummaryMessage() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(1L);
        SuggestionResponseDto req = SuggestionResponseDto.builder()
                .acceptedIds(List.of(10L))
                .declinedIds(List.of(11L))
                .build();
        SuggestionBatchResponseDto serviceResp = SuggestionBatchResponseDto.builder()
                .acceptedTasks(List.of(TaskDto.builder().taskId(20L).build()))
                .declined(List.of(SuggestedTasksDto.builder().taskId(11L).build()))
                .totalProcessed(2)
                .acceptedCount(1)
                .declinedCount(1)
                .build();
        when(suggestedTaskService.respondToSuggestions(any(SuggestionResponseDto.class), eq(1L))).thenReturn(serviceResp);

        mockMvc.perform(post("/api/v1/suggestions/respond")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Processed 2 suggestion(s): 1 accepted, 1 declined"));
    }

    @Test
    void pendingAllQuotaAndAnalytics_ShouldReturnOk() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(1L);
        when(suggestedTaskService.getPendingSuggestions(1L))
                .thenReturn(List.of(SuggestedTasksDto.builder().taskId(1L).taskName("A").build()));
        when(suggestedTaskService.getAllSuggestions(1L))
                .thenReturn(List.of(SuggestedTasksDto.builder().taskId(2L).taskName("B").build()));
        when(suggestedTaskService.getUserAnalytics(1L))
                .thenReturn(Map.of("totalSuggestions", 3L, "accepted", 1L));
        when(llmService.getRemainingMonthlyQuota(1L)).thenReturn(7);
        when(llmService.canUserRequestMoreSuggestions(1L)).thenReturn(true);

        mockMvc.perform(get("/api/v1/suggestions/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("1 pending suggestion(s)"));
        mockMvc.perform(get("/api/v1/suggestions/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("1 suggestion(s) found"));
        mockMvc.perform(get("/api/v1/suggestions/analytics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSuggestions").value(3));
        mockMvc.perform(get("/api/v1/suggestions/quota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.remainingThisMonth").value(7))
                .andExpect(jsonPath("$.data.canRequestMore").value(true));
    }

    @Test
    void singleAndBatchAcceptDecline_ShouldReturnOk() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(1L);
        TaskDto task = TaskDto.builder()
                .taskId(50L)
                .taskName("Accepted")
                .taskDeadline(LocalDateTime.now().plusDays(1))
                .build();
        SuggestedTasksDto declined = SuggestedTasksDto.builder()
                .taskId(60L)
                .taskName("Declined")
                .priority(Priority.MEDIUM)
                .suggestedStatus(SuggestedStatus.DECLINED)
                .build();
        when(suggestedTaskService.acceptSuggestion(5L, 1L)).thenReturn(task);
        when(suggestedTaskService.declineSuggestion(6L, 1L)).thenReturn(declined);
        when(suggestedTaskService.acceptMultipleSuggestions(List.of(7L, 8L), 1L)).thenReturn(List.of(task));

        mockMvc.perform(post("/api/v1/suggestions/5/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Suggestion accepted successfully"));
        mockMvc.perform(post("/api/v1/suggestions/6/decline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Suggestion declined"));
        mockMvc.perform(post("/api/v1/suggestions/accept-multiple")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(7L, 8L))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("1 suggestion(s) accepted"));

        verify(suggestedTaskService, times(1)).acceptSuggestion(5L, 1L);
        verify(suggestedTaskService, times(1)).declineSuggestion(6L, 1L);
    }

    static class TestSecurityUtils extends SecurityUtils {
        private Long id;

        TestSecurityUtils() {
            super(null);
        }

        void setAuthenticatedUserId(Long id) {
            this.id = id;
        }

        @Override
        public Long getAuthenticatedUserId() {
            return id;
        }
    }
}
