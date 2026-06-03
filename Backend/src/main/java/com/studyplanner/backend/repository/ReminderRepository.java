package com.studyplanner.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.studyplanner.backend.entity.Reminder;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {

    // Used by the scheduler, unsent reminders are sent by the scheduler

    @Query("SELECT r FROM Reminder r " +
            "JOIN FETCH r.task t " +
            "JOIN FETCH t.user u " +
            "WHERE r.reminderSent = false " +
            "AND r.reminderDate BETWEEN :from AND :to")
    List<Reminder> findUnsentRemindersInWindow(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    // Check if a reminder already exists for a task to avoid duplicates
    boolean existsByTaskIdAndReminderSentFalse(Long taskId);

    // Find all reminders for a task (userful for in-app notif)
    List<Reminder> findByTaskId(Long taskId);

    // Find all unsent in-app notifications for a user
    @Query("SELECT r FROM Reminder r " +
            "JOIN FETCH r.task t " +
            "WHERE t.user.id = :userId " +
            "AND r.reminderSent = false")
    List<Reminder> findUnsentRemindersByUserId(@Param("userId") Long userId);

}
