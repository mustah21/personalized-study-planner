package com.studyplanner.backend.service;

import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.User;

public interface EmailService {

    void sendTaskReminderEmail(String email, String firstName, Task task);

    void sendWelcomeEmail(String email, String firstName);

    void sendShareInviteEmail(String toEmail,
            String receiverFirstName,
            User sender,
            Task task,
            String acceptUrl,
            String declineUrl);
}
