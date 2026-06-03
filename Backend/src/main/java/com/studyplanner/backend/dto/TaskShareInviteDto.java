package com.studyplanner.backend.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.studyplanner.backend.entity.TaskShareInvite.InviteStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskShareInviteDto {

    private Long inviteId;
    private String inviteToken;
    private InviteStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;

    // Sender details shown to recipient
    private Long senderUserId;
    private String senderFullName;
    private String senderEmail;
    private String senderProfilePicture;

    // Task details shown in invite
    private Long taskId;
    private String taskName;
    private String taskDescription;
    private LocalDateTime taskDeadline;
}
