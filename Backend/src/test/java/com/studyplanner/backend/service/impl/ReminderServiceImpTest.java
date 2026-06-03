package com.studyplanner.backend.service.impl;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
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
import com.studyplanner.backend.repository.ReminderRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReminderServiceImp Tests")
class ReminderServiceImpTest {

    @Mock
    private ReminderRepository reminderRepository;

    @Test
    void createReminderForTask_ShouldCoverSkipAndSavePaths() {
        ReminderServiceImp service = new ReminderServiceImp(reminderRepository);

        Task noDeadline = Task.builder().id(1L).taskName("No deadline").build();
        service.createReminderForTask(noDeadline);
        verify(reminderRepository, never()).save(any(Reminder.class));

        Task duplicate = Task.builder().id(2L).taskName("Duplicate")
                .taskDeadline(LocalDateTime.now().plusDays(3)).build();
        when(reminderRepository.existsByTaskIdAndReminderSentFalse(2L)).thenReturn(true);
        service.createReminderForTask(duplicate);
        verify(reminderRepository, never()).save(any(Reminder.class));

        Task pastReminderDate = Task.builder().id(3L).taskName("Past")
                .taskDeadline(LocalDateTime.now().minusHours(1)).build();
        when(reminderRepository.existsByTaskIdAndReminderSentFalse(3L)).thenReturn(false);
        service.createReminderForTask(pastReminderDate);
        verify(reminderRepository, never()).save(any(Reminder.class));

        Task valid = Task.builder().id(4L).taskName("Valid")
                .taskDeadline(LocalDateTime.now().plusDays(2)).build();
        when(reminderRepository.existsByTaskIdAndReminderSentFalse(4L)).thenReturn(false);
        service.createReminderForTask(valid);
        verify(reminderRepository, times(1)).save(any(Reminder.class));
    }

    @Test
    void cancelReminderForTask_ShouldDeleteOnlyUnsent() {
        ReminderServiceImp service = new ReminderServiceImp(reminderRepository);
        Reminder unsent = Reminder.builder().id(1L).reminderSent(false).build();
        Reminder sent = Reminder.builder().id(2L).reminderSent(true).build();

        when(reminderRepository.findByTaskId(10L)).thenReturn(List.of(unsent, sent));
        doNothing().when(reminderRepository).deleteAll(List.of(unsent));

        service.cancelReminderForTask(10L);
        verify(reminderRepository, times(1)).deleteAll(List.of(unsent));
    }

    @Test
    void cancelReminderForTask_WhenNoUnsent_ShouldNotDelete() {
        ReminderServiceImp service = new ReminderServiceImp(reminderRepository);
        Reminder sent = Reminder.builder().id(2L).reminderSent(true).build();
        when(reminderRepository.findByTaskId(11L)).thenReturn(List.of(sent));

        service.cancelReminderForTask(11L);
        verify(reminderRepository, never()).deleteAll(any());
    }
}
