package com.studyplanner.backend.mapper;

import com.studyplanner.backend.dto.TaskShareInviteDto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.TaskShareInvite;
import com.studyplanner.backend.entity.User;

public class TaskShareInviteMapper {
    private TaskShareInviteMapper() {
        /* This utility class should not be instantiated */
    }


    public static TaskShareInviteDto mapToDto(TaskShareInvite invite) {
        User sender = invite.getSender();
        Task task = invite.getTask();

        return TaskShareInviteDto.builder()
                .inviteId(invite.getId())
                .inviteToken(invite.getInviteToken())
                .status(invite.getStatus())
                .createdAt(invite.getCreatedAt())
                .respondedAt(invite.getRespondedAt())

                // Sender details shown to recipient
                .senderUserId(sender.getId())
                .senderFullName(sender.getFirstName() + " " + sender.getLastName())
                .senderEmail(sender.getEmail())
                .senderProfilePicture(sender.getProfilePicture())

                // Task details — name, description, deadline only
                .taskId(task.getId())
                .taskName(task.getTaskName())
                .taskDescription(task.getTaskDescription())
                .taskDeadline(task.getTaskDeadline())
                .build();
    }
}
