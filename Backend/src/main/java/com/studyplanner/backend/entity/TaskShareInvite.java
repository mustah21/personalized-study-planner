package com.studyplanner.backend.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "task_share_invites")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TaskShareInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long id;

    // User who shared the task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    // User who received the invite
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // Task being shared
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InviteStatus status;

    public enum InviteStatus {
        PENDING,
        ACCEPTED,
        DECLINED
    }

    // secure token used in email links to accept/decline invites
    @Column(name = "invite_token", nullable = false, unique = true)
    private String inviteToken;

    // When receicer responds to the invite
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    // If accepted, when the task was shared with the receiver
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_task_id")
    private Task sharedTask;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
