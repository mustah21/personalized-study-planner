package com.studyplanner.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.studyplanner.backend.entity.Reminder;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.repository.ReminderRepository;
import com.studyplanner.backend.service.ReminderService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ReminderServiceImp implements ReminderService {

    private final ReminderRepository reminderRepository;

    @Override
    @Transactional
    public void createReminderForTask(Task task) {

        // Skip if no deadline is set
        if (task.getTaskDeadline() == null) {
            log.info("Task '{}' has no deadline - reminder skipped", task.getTaskName());
            return;
        }

        // Skip if reminder already exists
        if (reminderRepository.existsByTaskIdAndReminderSentFalse(task.getId())) {
            log.info("Reminder for task '{}' already exists - reminder skipped", task.getTaskName());
            return;
        }

        // Schedule reminder for exactly 1 day before deadline
        LocalDateTime reminderDate = task.getTaskDeadline().minusDays(1);

        // Do not schedule reminder if calculated date is in the past
        if (reminderDate.isBefore(LocalDateTime.now())) {
            log.info("Reminder date {} is in the past for the task: '{}' - skip", reminderDate, task.getTaskName());
            return;
        }

        Reminder reminder = Reminder.builder()
                .task(task)
                .reminderDate(reminderDate)
                .reminderSent(false)
                .build();

        reminderRepository.save(reminder);

        log.info("Reminder created for task '{}'", task.getTaskName());
    }

    @Override
    @Transactional
    public void cancelReminderForTask(Long taskId) {

        List<Reminder> reminders = reminderRepository.findByTaskId(taskId);
        List<Reminder> unsent = reminders.stream()
                .filter(r -> !r.isReminderSent())
                .toList();

        if (!unsent.isEmpty()) {
            reminderRepository.deleteAll(unsent);
            log.info("Cancelled {} unsent reminder(s) for task id={}", unsent.size(), taskId);
        }

    }

}
