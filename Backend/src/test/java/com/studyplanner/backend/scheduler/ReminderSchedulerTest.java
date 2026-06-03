package com.studyplanner.backend.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studyplanner.backend.entity.Reminder;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.repository.ReminderRepository;
import com.studyplanner.backend.service.EmailService;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderScheduler Tests")
class ReminderSchedulerTest {

    @Mock
    private EmailService emailService;
    @Mock
    private ReminderRepository reminderRepository;

    @Test
    void processReminders_WhenEmpty_ShouldReturnWithoutSending() {
        ReminderScheduler scheduler = new ReminderScheduler(emailService, reminderRepository);
        when(reminderRepository.findUnsentRemindersInWindow(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        scheduler.processReminders();

        verify(emailService, never()).sendTaskReminderEmail(any(), any(), any());
        verify(reminderRepository, never()).save(any(Reminder.class));
    }

    @Test
    void processReminders_ShouldSendAndMarkSent_AndContinueOnErrors() {
        ReminderScheduler scheduler = new ReminderScheduler(emailService, reminderRepository);

        User okUser = User.builder().firstName("Aashish").lastName("Dev").email("a@example.com").build();
        Task okTask = Task.builder().taskName("Revise").user(okUser).build();
        Reminder okReminder = Reminder.builder().id(1L).task(okTask).reminderSent(false).build();

        User badUser = User.builder().firstName("Rita").lastName("Fail").email("bad@example.com").build();
        Task badTask = Task.builder().taskName("Fail task").user(badUser).build();
        Reminder badReminder = Reminder.builder().id(2L).task(badTask).reminderSent(false).build();

        when(reminderRepository.findUnsentRemindersInWindow(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(okReminder, badReminder));
        org.mockito.Mockito.doAnswer(invocation -> {
            String email = invocation.getArgument(0);
            if ("bad@example.com".equals(email)) {
                throw new RuntimeException("smtp");
            }
            return null;
        }).when(emailService).sendTaskReminderEmail(any(), any(), any());

        scheduler.processReminders();

        verify(emailService, times(1)).sendTaskReminderEmail("a@example.com", "Aashish Dev", okTask);
        verify(emailService, times(1)).sendTaskReminderEmail("bad@example.com", "Rita Fail", badTask);
        verify(reminderRepository, times(1)).save(okReminder);
    }
}
