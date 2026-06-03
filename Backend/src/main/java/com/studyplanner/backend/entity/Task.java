package com.studyplanner.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tasks")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder

public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private List<Reminder> reminders;

    @Column(name = "task_name", nullable = false)
    private String taskName;

    @Column(name = "task_description", nullable = false)
    private String taskDescription;

    @Column(name = "task_deadline")
    private LocalDateTime taskDeadline;


    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    @Column(name = "priority")
    private Priority priority;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }

    @Column(name = "status")
    private Status status;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    // Email of the user who shared this task (null if created by owner)
    @Column(name = "shared_by_email")
    private String sharedByEmail;

    // New fields for LLM integration
    // Indicates if the task has been accepted from the LLM
    @Column(name = "from_llm_suggestion")
    private boolean fromLlmSuggestion;

    // If this task came from LLM, points to the suggested task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suggested_task_id")
    private SuggestedLLM suggestedTask;

    // Creation time stamp
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    // Updated time stamp
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @Getter
    public enum Language{
        EN("en"),
        FI("fi"),
        NE("ne"),
        VI("vi");

        private final String code;

        Language(String code) {
            this.code = code;
        }
        @JsonCreator
        public static Language fromCode(String value) {
            for (Language lang : values()) {
                if (lang.code.equalsIgnoreCase(value)) {
                    return lang;
                }
            }
            throw new IllegalArgumentException("Unknown language: " + value);
        }
    }

    // Language column showing the tasks saved language
    @Enumerated(EnumType.STRING)
    @Column(name="Language")
    private Language language;


}