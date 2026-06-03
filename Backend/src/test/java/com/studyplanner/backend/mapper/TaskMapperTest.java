package com.studyplanner.backend.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;
import com.studyplanner.backend.entity.User;

/**
 * Unit tests for TaskMapper.
 * Tests cover all mapping operations between Task entity and TaskDto.
 */
class TaskMapperTest {

	private Task testTask;
	private TaskDto testTaskDto;
	private User testUser;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.id(1L)
				.email("user@example.com")
				.firstName("John")
				.lastName("Doe")
				.build();

		testTask = Task.builder()
				.id(1L)
				.user(testUser)
				.taskName("Test Task")
				.taskDescription("This is a test task")
				.taskDeadline(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
				.priority(Priority.HIGH)
				.status(Status.PENDING)
				.completed(false)
				.sharedByEmail("sender@example.com")
				.createdAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
				.updatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
				.build();

		testTaskDto = TaskDto.builder()
				.taskId(1L)
				.userId(1L)
				.taskName("Test Task")
				.taskDescription("This is a test task")
				.taskDeadline(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
				.priority(Priority.HIGH)
				.status(Status.PENDING)
				.completed(false)
				.sharedByEmail("sender@example.com")
				.build();
	}

	@Nested
	@DisplayName("mapToTaskDto Tests")
	class MapToTaskDtoTests {

		@Test
		@DisplayName("Should successfully map Task entity to TaskDto")
		void mapToTaskDto_WithValidTask_ShouldReturnTaskDto() {
			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertNotNull(result);
			assertEquals(testTask.getId(), result.getTaskId());
			assertEquals(testTask.getUser().getId(), result.getUserId());
			assertEquals(testTask.getTaskName(), result.getTaskName());
			assertEquals(testTask.getTaskDescription(), result.getTaskDescription());
			assertEquals(testTask.getTaskDeadline(), result.getTaskDeadline());
			assertEquals(testTask.getPriority(), result.getPriority());
			assertEquals(testTask.getStatus(), result.getStatus());
			assertEquals(testTask.isCompleted(), result.isCompleted());
			assertEquals(testTask.getSharedByEmail(), result.getSharedByEmail());
		}

		@Test
		@DisplayName("Should map task with null shared email")
		void mapToTaskDto_WithNullSharedEmail_ShouldMapCorrectly() {
			// Arrange
			testTask.setSharedByEmail(null);

			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertNotNull(result);
			assertNull(result.getSharedByEmail());
		}

		@Test
		@DisplayName("Should map task with MEDIUM priority")
		void mapToTaskDto_WithMediumPriority_ShouldMapCorrectly() {
			// Arrange
			testTask.setPriority(Priority.MEDIUM);

			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertEquals(Priority.MEDIUM, result.getPriority());
		}

		@Test
		@DisplayName("Should map task with LOW priority")
		void mapToTaskDto_WithLowPriority_ShouldMapCorrectly() {
			// Arrange
			testTask.setPriority(Priority.LOW);

			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertEquals(Priority.LOW, result.getPriority());
		}

		@Test
		@DisplayName("Should map task with IN_PROGRESS status")
		void mapToTaskDto_WithInProgressStatus_ShouldMapCorrectly() {
			// Arrange
			testTask.setStatus(Status.IN_PROGRESS);

			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertEquals(Status.IN_PROGRESS, result.getStatus());
		}

		@Test
		@DisplayName("Should map task with COMPLETED status")
		void mapToTaskDto_WithCompletedStatus_ShouldMapCorrectly() {
			// Arrange
			testTask.setStatus(Status.COMPLETED);
			testTask.setCompleted(true);

			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertEquals(Status.COMPLETED, result.getStatus());
			assertTrue(result.isCompleted());
		}

		@Test
		@DisplayName("Should map completed task")
		void mapToTaskDto_WithCompletedTask_ShouldMapCompletedFlag() {
			// Arrange
			testTask.setCompleted(true);

			// Act
			TaskDto result = TaskMapper.mapToTaskDto(testTask);

			// Assert
			assertTrue(result.isCompleted());
		}
	}

	@Nested
	@DisplayName("mapToTask Tests")
	class MapToTaskTests {

		@Test
		@DisplayName("Should successfully map TaskDto to Task entity")
		void mapToTask_WithValidTaskDto_ShouldReturnTask() {
			// Act
			Task result = TaskMapper.mapToTask(testTaskDto, testUser);

			// Assert
			assertNotNull(result);
			assertEquals(testTaskDto.getTaskId(), result.getId());
			assertEquals(testTaskDto.getTaskName(), result.getTaskName());
			assertEquals(testTaskDto.getTaskDescription(), result.getTaskDescription());
			assertEquals(testTaskDto.getTaskDeadline(), result.getTaskDeadline());
			assertEquals(testTaskDto.getPriority(), result.getPriority());
			assertEquals(testTaskDto.getStatus(), result.getStatus());
			assertEquals(testTaskDto.isCompleted(), result.isCompleted());
			assertEquals(testUser, result.getUser());
		}

		@Test
		@DisplayName("Should map task with null task id")
		void mapToTask_WithNullTaskId_ShouldMapCorrectly() {
			// Arrange
			testTaskDto.setTaskId(null);

			// Act
			Task result = TaskMapper.mapToTask(testTaskDto, testUser);

			// Assert
			assertNull(result.getId());
		}

		@Test
		@DisplayName("Should map task with different priority levels")
		void mapToTask_WithVariousPriorities_ShouldMapCorrectly() {
			// Act & Assert for HIGH
			testTaskDto.setPriority(Priority.HIGH);
			Task highPriorityTask = TaskMapper.mapToTask(testTaskDto, testUser);
			assertEquals(Priority.HIGH, highPriorityTask.getPriority());

			// Act & Assert for MEDIUM
			testTaskDto.setPriority(Priority.MEDIUM);
			Task mediumPriorityTask = TaskMapper.mapToTask(testTaskDto, testUser);
			assertEquals(Priority.MEDIUM, mediumPriorityTask.getPriority());

			// Act & Assert for LOW
			testTaskDto.setPriority(Priority.LOW);
			Task lowPriorityTask = TaskMapper.mapToTask(testTaskDto, testUser);
			assertEquals(Priority.LOW, lowPriorityTask.getPriority());
		}
	}

	@Nested
	@DisplayName("updateTask Tests")
	class UpdateTaskTests {

		@Test
		@DisplayName("Should successfully update task fields from TaskDto")
		void updateTask_WithValidTaskDto_ShouldUpdateAllFields() {
			// Arrange
			TaskDto updateDto = TaskDto.builder()
					.taskId(1L)
					.userId(1L)
					.taskName("Updated Task Name")
					.taskDescription("Updated description")
					.taskDeadline(LocalDateTime.of(2025, 6, 15, 12, 0, 0))
					.priority(Priority.LOW)
					.status(Status.COMPLETED)
					.completed(true)
					.build();

			// Act
			TaskMapper.updateTask(testTask, updateDto);

			// Assert
			assertEquals("Updated Task Name", testTask.getTaskName());
			assertEquals("Updated description", testTask.getTaskDescription());
			assertEquals(LocalDateTime.of(2025, 6, 15, 12, 0, 0), testTask.getTaskDeadline());
			assertEquals(Priority.LOW, testTask.getPriority());
			assertEquals(Status.COMPLETED, testTask.getStatus());
			assertTrue(testTask.isCompleted());
		}

		@Test
		@DisplayName("Should update task priority only")
		void updateTask_WithPriorityChange_ShouldUpdatePriority() {
			// Arrange
			TaskDto updateDto = TaskDto.builder()
					.taskName(testTask.getTaskName())
					.taskDescription(testTask.getTaskDescription())
					.taskDeadline(testTask.getTaskDeadline())
					.priority(Priority.MEDIUM)
					.status(testTask.getStatus())
					.completed(testTask.isCompleted())
					.build();

			// Act
			TaskMapper.updateTask(testTask, updateDto);

			// Assert
			assertEquals(Priority.MEDIUM, testTask.getPriority());
			assertEquals(testTask.getTaskName(), testTask.getTaskName());
		}

		@Test
		@DisplayName("Should update task status and completed flag")
		void updateTask_WithStatusChange_ShouldUpdateStatusAndCompleted() {
			// Arrange
			TaskDto updateDto = TaskDto.builder()
					.taskName(testTask.getTaskName())
					.taskDescription(testTask.getTaskDescription())
					.taskDeadline(testTask.getTaskDeadline())
					.priority(testTask.getPriority())
					.status(Status.IN_PROGRESS)
					.completed(false)
					.build();

			// Act
			TaskMapper.updateTask(testTask, updateDto);

			// Assert
			assertEquals(Status.IN_PROGRESS, testTask.getStatus());
			assertFalse(testTask.isCompleted());
		}

		@Test
		@DisplayName("Should update task name and description only")
		void updateTask_WithNameAndDescriptionChange_ShouldUpdateBoth() {
			// Arrange
			TaskDto updateDto = TaskDto.builder()
					.taskName("New Name")
					.taskDescription("New Description")
					.taskDeadline(testTask.getTaskDeadline())
					.priority(testTask.getPriority())
					.status(testTask.getStatus())
					.completed(testTask.isCompleted())
					.build();

			// Act
			TaskMapper.updateTask(testTask, updateDto);

			// Assert
			assertEquals("New Name", testTask.getTaskName());
			assertEquals("New Description", testTask.getTaskDescription());
		}

		@Test
		@DisplayName("Should update task deadline")
		void updateTask_WithDeadlineChange_ShouldUpdateDeadline() {
			// Arrange
			LocalDateTime newDeadline = LocalDateTime.of(2025, 12, 25, 15, 30, 0);
			TaskDto updateDto = TaskDto.builder()
					.taskName(testTask.getTaskName())
					.taskDescription(testTask.getTaskDescription())
					.taskDeadline(newDeadline)
					.priority(testTask.getPriority())
					.status(testTask.getStatus())
					.completed(testTask.isCompleted())
					.build();

			// Act
			TaskMapper.updateTask(testTask, updateDto);

			// Assert
			assertEquals(newDeadline, testTask.getTaskDeadline());
		}
	}
}
//commit
