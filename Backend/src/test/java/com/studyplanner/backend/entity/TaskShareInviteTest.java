package com.studyplanner.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskShareInviteTest {

    @Test
    void builderAndGetters() {
        LocalDateTime responded = LocalDateTime.of(2023, 1, 2, 3, 4);
        TaskShareInvite invite = TaskShareInvite.builder()
                .id(1L)
                .status(TaskShareInvite.InviteStatus.PENDING)
                .inviteToken("tok-123")
                .respondedAt(responded)
                .build();

        assertEquals(1L, invite.getId());
        assertEquals(TaskShareInvite.InviteStatus.PENDING, invite.getStatus());
        assertEquals("tok-123", invite.getInviteToken());
        assertEquals(responded, invite.getRespondedAt());
    }

    @Test
    void enumContainsExpectedValues() {
        TaskShareInvite.InviteStatus[] vals = TaskShareInvite.InviteStatus.values();
        assertEquals(3, vals.length);

        boolean hasPending = false, hasAccepted = false, hasDeclined = false;
        for (TaskShareInvite.InviteStatus v : vals) {
            if (v == TaskShareInvite.InviteStatus.PENDING) hasPending = true;
            if (v == TaskShareInvite.InviteStatus.ACCEPTED) hasAccepted = true;
            if (v == TaskShareInvite.InviteStatus.DECLINED) hasDeclined = true;
        }
        assertTrue(hasPending && hasAccepted && hasDeclined);
    }

    @Test
    void equalsAndHashCode_consistentForSameData() {
        TaskShareInvite a = TaskShareInvite.builder()
                .id(5L)
                .inviteToken("t")
                .status(TaskShareInvite.InviteStatus.ACCEPTED)
                .build();
        TaskShareInvite b = TaskShareInvite.builder()
                .id(5L)
                .inviteToken("t")
                .status(TaskShareInvite.InviteStatus.ACCEPTED)
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_containsClassName() {
        TaskShareInvite t = TaskShareInvite.builder().id(2L).inviteToken("x").build();
        assertTrue(t.toString().contains("TaskShareInvite"));
    }
}

