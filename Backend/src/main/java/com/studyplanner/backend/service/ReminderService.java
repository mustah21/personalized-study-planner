package com.studyplanner.backend.service;

import com.studyplanner.backend.entity.Task;

public interface ReminderService {

    // create a new reminder for a task
    void createReminderForTask(Task task);

    // cancel a reminder for a task incase the task is deleted or deadline is
    // changed or completed
    void cancelReminderForTask(Long taskId);
}
