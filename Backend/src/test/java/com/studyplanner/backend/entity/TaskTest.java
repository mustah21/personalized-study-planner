package com.studyplanner.backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    void builderAndGetters() {
        LocalDateTime deadline = LocalDateTime.of(2024, 5, 6, 12, 0);
        Task t = Task.builder()
                .id(10L)
                .taskName("Write tests")
                .taskDescription("Create unit tests for Task entity")
                .taskDeadline(deadline)
                .priority(Task.Priority.MEDIUM)
                .status(Task.Status.PENDING)
                .completed(false)
                .sharedByEmail("owner@example.com")
                .fromLlmSuggestion(false)
                .build();

        assertEquals(10L, t.getId());
        assertEquals("Write tests", t.getTaskName());
        assertEquals("Create unit tests for Task entity", t.getTaskDescription());
        assertEquals(deadline, t.getTaskDeadline());
        assertEquals(Task.Priority.MEDIUM, t.getPriority());
        assertEquals(Task.Status.PENDING, t.getStatus());
        assertFalse(t.isCompleted());
        assertEquals("owner@example.com", t.getSharedByEmail());
        assertFalse(t.isFromLlmSuggestion());

        // createdAt / updatedAt are populated by Hibernate; in unit tests they should be null
        assertNull(t.getCreatedAt());
        assertNull(t.getUpdatedAt());
    }

    @Test
    void enumContainsExpectedValues() {
        Task.Priority[] p = Task.Priority.values();
        assertEquals(3, p.length);
        assertArrayEquals(new Task.Priority[]{Task.Priority.LOW, Task.Priority.MEDIUM, Task.Priority.HIGH}, p);

        Task.Status[] s = Task.Status.values();
        assertEquals(3, s.length);
        assertArrayEquals(new Task.Status[]{Task.Status.PENDING, Task.Status.IN_PROGRESS, Task.Status.COMPLETED}, s);
    }

    @Test
    void equalsAndHashCode_consistentForSameData() {
        Task a = Task.builder()
                .id(1L)
                .taskName("A")
                .taskDescription("desc")
                .build();
        Task b = Task.builder()
                .id(1L)
                .taskName("A")
                .taskDescription("desc")
                .build();

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }


}

