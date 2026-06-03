package com.studyplanner.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplanner.backend.dto.LlmTaskGenerationRequest;
import com.studyplanner.backend.dto.LlmTaskGenerationResponse;
import com.studyplanner.backend.entity.SuggestedLLM;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.LlmResponseParseException;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.repository.SuggestedTaskRepository;
import com.studyplanner.backend.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("LlmServiceImpl Tests")
class LlmServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;
    @Mock
    private ChatClient.Builder chatClientBuilder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SuggestedTaskRepository suggestedTaskRepository;

    private LlmServiceImpl llmService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        llmService = new LlmServiceImpl(
                chatClientBuilder, userRepository, suggestedTaskRepository, new ObjectMapper(), 5);
    }

    @Test
    void getRemainingAndCanRequest_ShouldRespectQuotaBoundaries() {
        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(2L);
        assertEquals(3, llmService.getRemainingMonthlyQuota(1L));
        assertTrue(llmService.canUserRequestMoreSuggestions(1L));

        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(10L);
        assertEquals(0, llmService.getRemainingMonthlyQuota(1L));
        assertFalse(llmService.canUserRequestMoreSuggestions(1L));
    }

    @Test
    void generateTaskSuggestions_ShouldParseAndSaveTasks() {
        User user = User.builder().id(1L).email("u@example.com").build();
        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(0L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        String rawJson = """
                {
                  "tasks":[
                    {
                      "taskName":"Read chapter",
                      "taskDescription":"Study chapter thoroughly",
                      "taskDeadline":"2030-01-10T10:00:00",
                      "priority":"HIGH"
                    }
                  ]
                }
                """;
        when(chatClient.prompt().system(any(String.class)).user(any(String.class)).call().content()).thenReturn(rawJson);
        when(suggestedTaskRepository.save(any(SuggestedLLM.class))).thenAnswer(inv -> {
            SuggestedLLM e = inv.getArgument(0);
            e.setId(11L);
            e.setCreatedAt(LocalDateTime.now());
            return e;
        });

        LlmTaskGenerationRequest req = LlmTaskGenerationRequest.builder()
                .prompt("help me plan")
                .language("en")
                .build();
        LlmTaskGenerationResponse response = llmService.generateTaskSuggestions(req, 1L);

        assertNotNull(response);
        assertEquals(1, response.getTotalGenerated());
        assertTrue(response.getMessage().contains("Generated 1 task suggestions"));
        assertEquals("Read chapter", response.getSuggestions().get(0).getTaskName());
    }

    @Test
    void generateTaskSuggestions_WhenQuotaExceeded_ShouldThrow() {
        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(99L);
        LlmTaskGenerationRequest req = LlmTaskGenerationRequest.builder().prompt("plan").language("en").build();

        assertThrows(IllegalStateException.class, () -> llmService.generateTaskSuggestions(req, 1L));
    }

    @Test
    void generateTaskSuggestions_WhenUserMissing_ShouldThrow() {
        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(0L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        LlmTaskGenerationRequest req = LlmTaskGenerationRequest.builder().prompt("plan").language("en").build();
        assertThrows(ResourceNotFoundException.class, () -> llmService.generateTaskSuggestions(req, 1L));
    }

    @Test
    void generateTaskSuggestions_WhenLlmReturnsInvalidJson_ShouldThrow() {
        User user = User.builder().id(1L).email("u@example.com").build();
        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(0L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatClient.prompt().system(any(String.class)).user(any(String.class)).call().content())
                .thenReturn("not-json");

        LlmTaskGenerationRequest req = LlmTaskGenerationRequest.builder()
                .prompt("plan")
                .language("fi")
                .build();
        assertThrows(LlmResponseParseException.class, () -> llmService.generateTaskSuggestions(req, 1L));
    }

    @Test
    void generateTaskSuggestions_WhenAllTasksInvalid_ShouldThrowCustomParseException() {
        User user = User.builder().id(1L).email("u@example.com").build();
        when(suggestedTaskRepository.countByUserIdAndTaskDeadlineBetween(anyLong(), any(), any())).thenReturn(0L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(chatClient.prompt().system(any(String.class)).user(any(String.class)).call().content())
                .thenReturn("""
                        {
                          "tasks":[
                            {
                              "taskName":"Bad task",
                              "taskDescription":"Invalid date",
                              "taskDeadline":"not-a-date",
                              "priority":"MEDIUM"
                            }
                          ]
                        }
                        """);

        LlmTaskGenerationRequest req = LlmTaskGenerationRequest.builder()
                .prompt("plan")
                .language("en")
                .build();
        assertThrows(LlmResponseParseException.class, () -> llmService.generateTaskSuggestions(req, 1L));
    }

    @Test
    void chat_ShouldThrowUntilImplemented() {
        assertThrows(UnsupportedOperationException.class,
                () -> llmService.chat(new LlmTaskGenerationRequest("p", null, false, "en")));
    }
}
