package com.studyplanner.backend.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyplanner.backend.dto.SuggestedTasksDto;
import com.studyplanner.backend.dto.SuggestionBatchResponseDto;
import com.studyplanner.backend.dto.SuggestionResponseDto;
import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.SuggestedLLM;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.SuggestedLLM.SuggestedStatus;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.mapper.SuggestedTasksMapper;
import com.studyplanner.backend.mapper.TaskMapper;
import com.studyplanner.backend.repository.SuggestedTaskRepository;
import com.studyplanner.backend.repository.TaskRepository;
import com.studyplanner.backend.service.ReminderService;
import com.studyplanner.backend.service.SuggestedTaskService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SuggestedTaskServiceImpl implements SuggestedTaskService {

    private final SuggestedTaskRepository suggestedTaskRepository;
    private final TaskRepository taskRepository;
    private final ReminderService reminderService;
    private final SuggestedTaskService transactionalService;

    @Autowired
    public SuggestedTaskServiceImpl(
            SuggestedTaskRepository suggestedTaskRepository,
            TaskRepository taskRepository,
            ReminderService reminderService,
            @Lazy SuggestedTaskService transactionalService) {
        this.suggestedTaskRepository = suggestedTaskRepository;
        this.taskRepository = taskRepository;
        this.reminderService = reminderService;
        this.transactionalService = transactionalService;
    }

    // Test-friendly constructor
    SuggestedTaskServiceImpl(
            SuggestedTaskRepository suggestedTaskRepository,
            TaskRepository taskRepository,
            ReminderService reminderService) {
        this.suggestedTaskRepository = suggestedTaskRepository;
        this.taskRepository = taskRepository;
        this.reminderService = reminderService;
        this.transactionalService = null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SuggestedTasksDto> getPendingSuggestions(Long userId) {
        return suggestedTaskRepository.findByUserIdAndSuggestedStatus(userId, SuggestedStatus.PENDING)
                .stream()
                .map(SuggestedTasksMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public TaskDto acceptSuggestion(Long suggestionId, Long userId) {
        SuggestedLLM suggestion = suggestedTaskRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not Found"));

        if (!suggestion.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("Unauthorized Access! You don't have premission to access this");
        }

        if (suggestion.getSuggestedStatus() != SuggestedStatus.PENDING) {
            throw new IllegalStateException("This suggestion has already been " +
                    suggestion.getSuggestedStatus().name().toLowerCase());
        }

        // copy to main task table
        Task task = Task.builder()
                .user(suggestion.getUser())
                .taskName(suggestion.getTaskName())
                .taskDescription(suggestion.getTaskDescription())
                .taskDeadline(suggestion.getTaskDeadline())
                .priority(Task.Priority.valueOf(suggestion.getPriority().name()))
                .status(Task.Status.PENDING) // default status
                .completed(false)
                .fromLlmSuggestion(true)
                .suggestedTask(suggestion)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Task savedTask = taskRepository.save(task);
        reminderService.createReminderForTask(savedTask);

        // Upadate suggestion record
        suggestion.setSuggestedStatus(SuggestedStatus.ACCEPTED);
        suggestion.setRespondedAt(LocalDateTime.now());
        suggestion.setAcceptedTask(savedTask);
        // keep the task work as pending
        suggestedTaskRepository.save(suggestion);

        log.info("User {} accepted suggestion {} -> created task {}",
                userId, suggestionId, savedTask.getId());

        return TaskMapper.mapToTaskDto(savedTask);
    }

    @Override
    @Transactional
    public SuggestedTasksDto declineSuggestion(Long suggestionId, Long userId) {
        SuggestedLLM suggestion = suggestedTaskRepository.findById(suggestionId)
                .orElseThrow(() -> new ResourceNotFoundException("Not Found"));

        if (!suggestion.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access");
        }

        if (suggestion.getSuggestedStatus() != SuggestedStatus.PENDING) {
            throw new IllegalStateException("This suggestion has already been " +
                    suggestion.getSuggestedStatus().name().toLowerCase());
        }

        suggestion.setSuggestedStatus(SuggestedStatus.DECLINED);
        suggestion.setRespondedAt(LocalDateTime.now());
        // task status doesnt matter for declined task
        SuggestedLLM updated = suggestedTaskRepository.save(suggestion);

        log.info("User {} declined suggestion {}", userId, suggestionId);
        return SuggestedTasksMapper.toDto(updated);
    }

    // Single batch endpoint for accept AND decline
    @Override
    @Transactional
    public SuggestionBatchResponseDto respondToSuggestions(
            SuggestionResponseDto response, Long userId) {

        List<TaskDto> acceptedTasks = new ArrayList<>();
        List<SuggestedTasksDto> declinedSuggestions = new ArrayList<>();

        // Process acceptances
        if (response.getAcceptedIds() != null && !response.getAcceptedIds().isEmpty()) {
            for (Long id : response.getAcceptedIds()) {
                try {
                    acceptedTasks.add(getTransactionalDelegate().acceptSuggestion(id, userId));
                } catch (Exception e) {
                    log.warn("Failed to accept suggestion {}: {}", id, e.getMessage());
                    // Continue processing other IDs even if one fails
                }
            }
        }

        // Process declines
        if (response.getDeclinedIds() != null && !response.getDeclinedIds().isEmpty()) {
            for (Long id : response.getDeclinedIds()) {
                try {
                    declinedSuggestions.add(getTransactionalDelegate().declineSuggestion(id, userId));
                } catch (Exception e) {
                    log.warn("Failed to decline suggestion {}: {}", id, e.getMessage());
                    // Continue processing other IDs even if one fails
                }
            }
        }

        return SuggestionBatchResponseDto.builder()
                .acceptedTasks(acceptedTasks)
                .declined(declinedSuggestions)
                .totalProcessed(acceptedTasks.size() + declinedSuggestions.size())
                .acceptedCount(acceptedTasks.size())
                .declinedCount(declinedSuggestions.size())
                .build();
    }

    @Override
    @Transactional
    public List<TaskDto> acceptMultipleSuggestions(List<Long> suggestionIds, Long userId) {
        List<TaskDto> acceptedTasks = new ArrayList<>();
        for (Long id : suggestionIds) {
            try {
                acceptedTasks.add(getTransactionalDelegate().acceptSuggestion(id, userId));
            } catch (Exception e) {
                log.warn("Failed to accept suggestion {}: {}", id, e.getMessage());
            }
        }
        return acceptedTasks;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserAnalytics(Long userId) {
        long total = suggestedTaskRepository.findByUserId(userId).size();
        long accepted = suggestedTaskRepository.countByUserIdAndSuggestedStatus(userId, SuggestedStatus.ACCEPTED);
        long declined = suggestedTaskRepository.countByUserIdAndSuggestedStatus(userId, SuggestedStatus.DECLINED);
        long pending = suggestedTaskRepository.countByUserIdAndSuggestedStatus(userId, SuggestedStatus.PENDING);

        Double acceptanceRate = suggestedTaskRepository.getAcceptanceRateByUserId(userId);
        acceptanceRate = acceptanceRate == null ? 0.0 : acceptanceRate;

        Map<String, Object> analytics = new HashMap<>();
        analytics.put("totalSuggestions", total);
        analytics.put("accepted", accepted);
        analytics.put("declined", declined);
        analytics.put("pending", pending);
        analytics.put("acceptanceRate", Math.round(acceptanceRate * 100));
        analytics.put("mostRecentSuggestions",
                suggestedTaskRepository.findRecentSuggestionsByUserId(
                                userId, LocalDateTime.now().minusDays(30))
                        .stream()
                        .map(SuggestedTasksMapper::toDto)
                        .collect(Collectors.toList()));

        return analytics;

    }

    @Override
    @Transactional(readOnly = true)
    public List<SuggestedTasksDto> getAllSuggestions(Long userId) {
        return suggestedTaskRepository.findByUserId(userId)
                .stream()
                .map(SuggestedTasksMapper::toDto)
                .collect(Collectors.toList());
    }

    private SuggestedTaskService getTransactionalDelegate() {
        return transactionalService != null ? transactionalService : this;
    }

}