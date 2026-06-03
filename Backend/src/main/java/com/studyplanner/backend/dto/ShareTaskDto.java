package com.studyplanner.backend.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
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
public class ShareTaskDto {

    private Long senderUserId;

    @NotEmpty(message = "Receiver is required")
    private List<Long> receiverUserIds;

    @NotEmpty(message = "Task is required")
    private List<Long> taskIds;
}
