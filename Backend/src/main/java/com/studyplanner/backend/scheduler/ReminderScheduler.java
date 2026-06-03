package com.studyplanner.backend.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.studyplanner.backend.entity.Reminder;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.repository.ReminderRepository;
import com.studyplanner.backend.service.EmailService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final EmailService emailService;
    private final ReminderRepository reminderRepository;

    // Run every day at server time of 8:00 Am
    // Find all unsent reminders with in that time

    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void processReminders() {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusHours(1); // to avoid double sending

        log.info("Processing reminders for window: {} to {}", now, now, windowEnd);

        List<Reminder> reminders = reminderRepository.findUnsentRemindersInWindow(now, windowEnd);

        if (reminders.isEmpty()) {
            log.info("No reminders to send ");
            return;
        }

        for (Reminder reminder : reminders) {
            try {
                Task task = reminder.getTask();
                User user = task.getUser();

                String fullName = user.getFirstName() + " " + user.getLastName();

                // send enail
                emailService.sendTaskReminderEmail(user.getEmail(), fullName, task);

                // Mark reminders as a sent
                reminder.setReminderSent(true);
                reminder.setSentAt(LocalDateTime.now());
                reminderRepository.save(reminder);

                log.info("Proccessed reminder id={} for task='{}' user='{}'", reminder.getId(), task.getTaskName(),
                        user.getEmail());
            } catch (Exception e) {
                log.error("Failed to process reminder id={}: {} ", reminder.getId(),
                        e.getMessage());
            }
        }

        log.info("Reminder scheduler completed. Processed {} reminders.", reminders.size());
    }

}
