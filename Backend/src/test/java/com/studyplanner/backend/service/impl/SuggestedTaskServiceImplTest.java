package com.studyplanner.backend.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studyplanner.backend.dto.SuggestedTasksDto;
import com.studyplanner.backend.dto.SuggestionBatchResponseDto;
import com.studyplanner.backend.dto.SuggestionResponseDto;
import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.SuggestedLLM;
import com.studyplanner.backend.entity.SuggestedLLM.SuggestedStatus;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.repository.SuggestedTaskRepository;
import com.studyplanner.backend.repository.TaskRepository;
import com.studyplanner.backend.service.ReminderService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuggestedTaskServiceImpl Tests")
class SuggestedTaskServiceImplTest {

    @Mock
    private SuggestedTaskRepository suggestedTaskRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ReminderService reminderService;

    private SuggestedTaskServiceImpl service;
    private User owner;
    private SuggestedLLM pendingSuggestion;

    @BeforeEach
    void setUp() {
        service = new SuggestedTaskServiceImpl(suggestedTaskRepository, taskRepository, reminderService);
        owner = User.builder().id(1L).email("owner@example.com").build();
        pendingSuggestion = SuggestedLLM.builder()
                .id(100L)
                .user(owner)
                .taskName("Read chapter")
                .taskDescription("Read chapter 3")
                .taskDeadline(LocalDateTime.now().plusDays(1))
                .priority(SuggestedLLM.Priority.HIGH)
                .suggestedStatus(SuggestedStatus.PENDING)
                .build();
    }

    @Test
    void getPendingAndAll_ShouldMapSuggestions() {
        when(suggestedTaskRepository.findByUserIdAndSuggestedStatus(1L, SuggestedStatus.PENDING))
                .thenReturn(List.of(pendingSuggestion));
        when(suggestedTaskRepository.findByUserId(1L)).thenReturn(List.of(pendingSuggestion));

        List<SuggestedTasksDto> pending = service.getPendingSuggestions(1L);
        List<SuggestedTasksDto> all = service.getAllSuggestions(1L);

        assertEquals(1, pending.size());
        assertEquals("Read chapter", pending.get(0).getTaskName());
        assertEquals(1, all.size());
    }

    @Test
    void acceptSuggestion_ShouldCreateTaskAndUpdateSuggestion() {
        when(suggestedTaskRepository.findById(100L)).thenReturn(Optional.of(pendingSuggestion));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.setId(500L);
            return t;
        });

        TaskDto result = service.acceptSuggestion(100L, 1L);

        assertEquals("Read chapter", result.getTaskName());
        assertEquals(Task.Status.PENDING, result.getStatus());
        assertFalse(result.isCompleted());
        verify(reminderService, times(1)).createReminderForTask(any(Task.class));
        verify(suggestedTaskRepository, times(1)).save(pendingSuggestion);
        assertEquals(SuggestedStatus.ACCEPTED, pendingSuggestion.getSuggestedStatus());
        assertEquals(500L, pendingSuggestion.getAcceptedTask().getId());
    }

    @Test
    void acceptSuggestion_UnauthorizedOrMissingOrAlreadyHandled_ShouldThrow() {
        SuggestedLLM foreign = SuggestedLLM.builder()
                .id(101L)
                .user(User.builder().id(9L).build())
                .suggestedStatus(SuggestedStatus.PENDING)
                .build();
        SuggestedLLM accepted = SuggestedLLM.builder()
                .id(102L)
                .user(owner)
                .suggestedStatus(SuggestedStatus.ACCEPTED)
                .build();

        when(suggestedTaskRepository.findById(999L)).thenReturn(Optional.empty());
        when(suggestedTaskRepository.findById(101L)).thenReturn(Optional.of(foreign));
        when(suggestedTaskRepository.findById(102L)).thenReturn(Optional.of(accepted));

        assertThrows(ResourceNotFoundException.class, () -> service.acceptSuggestion(999L, 1L));
        assertThrows(UnauthorizedAccessException.class, () -> service.acceptSuggestion(101L, 1L));
        assertThrows(IllegalStateException.class, () -> service.acceptSuggestion(102L, 1L));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void declineSuggestion_ShouldUpdateStatus() {
        when(suggestedTaskRepository.findById(100L)).thenReturn(Optional.of(pendingSuggestion));
        when(suggestedTaskRepository.save(any(SuggestedLLM.class))).thenAnswer(inv -> inv.getArgument(0));

        SuggestedTasksDto declined = service.declineSuggestion(100L, 1L);

        assertEquals(SuggestedStatus.DECLINED, declined.getSuggestedStatus());
        verify(suggestedTaskRepository, times(1)).save(pendingSuggestion);
    }

    @Test
    void respondAndAcceptMultiple_ShouldContinueOnFailures() {
        SuggestedTaskServiceImpl spyService = spy(new SuggestedTaskServiceImpl(
                suggestedTaskRepository, taskRepository, reminderService));

        doReturn(TaskDto.builder().taskId(1L).build()).when(spyService).acceptSuggestion(10L, 1L);
        doThrow(new IllegalStateException("bad")).when(spyService).acceptSuggestion(11L, 1L);
        doReturn(SuggestedTasksDto.builder().taskId(12L).build()).when(spyService).declineSuggestion(12L, 1L);

        SuggestionResponseDto response = SuggestionResponseDto.builder()
                .acceptedIds(List.of(10L, 11L))
                .declinedIds(List.of(12L))
                .build();
        SuggestionBatchResponseDto batch = spyService.respondToSuggestions(response, 1L);

        assertEquals(1, batch.getAcceptedCount());
        assertEquals(1, batch.getDeclinedCount());
        assertEquals(2, batch.getTotalProcessed());

        doReturn(TaskDto.builder().taskId(2L).build()).when(spyService).acceptSuggestion(20L, 1L);
        doThrow(new IllegalArgumentException("skip")).when(spyService).acceptSuggestion(21L, 1L);
        List<TaskDto> accepted = spyService.acceptMultipleSuggestions(List.of(20L, 21L), 1L);
        assertEquals(1, accepted.size());
    }

    @Test
    void getUserAnalytics_ShouldReturnComputedFields() {
        when(suggestedTaskRepository.findByUserId(1L)).thenReturn(List.of(pendingSuggestion, pendingSuggestion));
        when(suggestedTaskRepository.countByUserIdAndSuggestedStatus(1L, SuggestedStatus.ACCEPTED)).thenReturn(1L);
        when(suggestedTaskRepository.countByUserIdAndSuggestedStatus(1L, SuggestedStatus.DECLINED)).thenReturn(1L);
        when(suggestedTaskRepository.countByUserIdAndSuggestedStatus(1L, SuggestedStatus.PENDING)).thenReturn(0L);
        when(suggestedTaskRepository.getAcceptanceRateByUserId(1L)).thenReturn(0.75d);
        when(suggestedTaskRepository.findRecentSuggestionsByUserId(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(pendingSuggestion));

        Map<String, Object> analytics = service.getUserAnalytics(1L);
        assertEquals(2L, analytics.get("totalSuggestions"));
        assertEquals(75L, analytics.get("acceptanceRate"));
        assertTrue(((List<?>) analytics.get("mostRecentSuggestions")).size() == 1);

        ArgumentCaptor<LocalDateTime> sinceCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(suggestedTaskRepository).findRecentSuggestionsByUserId(eq(1L), sinceCaptor.capture());
        assertNotFuture(sinceCaptor.getValue());
    }

    private void assertNotFuture(LocalDateTime time) {
        assertTrue(time.isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
