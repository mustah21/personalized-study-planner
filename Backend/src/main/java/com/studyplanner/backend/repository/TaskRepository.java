package com.studyplanner.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // All tasks for a user
    List<Task> findByUserId(Long userId);

    // Filter by priority
    List<Task> findByUserIdAndPriority(Long userId, Priority priority);

    //Filter By status
    List<Task> findByUserIdAndStatus(Long userId, Status status);

    //Filter By completed status
    List<Task> findByUserIdAndCompleted(Long userId, boolean completed);

    // by date range
    List<Task> findByUserIdAndTaskDeadlineBetween(Long userId, LocalDateTime start, LocalDateTime end);

    // by deadline
    List<Task> findByUserIdAndTaskDeadline(Long userId, LocalDateTime taskDeadline);

    // by task name
    List<Task> findByUserIdAndTaskName(Long userId, String taskName);

    // by status and priority
    List<Task> findByUserIdAndStatusAndPriority(Long userId, Status status, Priority priority);


}
