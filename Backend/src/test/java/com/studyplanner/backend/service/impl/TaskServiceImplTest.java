package com.studyplanner.backend.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;
import com.studyplanner.backend.entity.User;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.repository.TaskRepository;
import com.studyplanner.backend.repository.UserRepository;
import com.studyplanner.backend.service.ReminderService;

/**
 * Unit tests for TaskServiceImpl.
 * Tests cover task creation, retrieval, updates, filtering, and deletion
 * operations.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ReminderService reminderService;

	@Mock
	private CalendarServiceImpl calendarService;

	@InjectMocks
	private TaskServiceImpl taskService;

	private User testUser;
	private Task testTask;
	private TaskDto testTaskDto;

	@BeforeEach
	void setUp() {
		testUser = User.builder()
				.id(1L)
				.email("user@example.com")
				.password("hashedPassword")
				.firstName("John")
				.lastName("Doe")
				.createdAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
				.updatedAt(LocalDateTime.of(2024, 1, 1, 10, 0, 0))
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
				.build();
	}

	@Nested
	@DisplayName("createTask Tests")
	class CreateTaskTests {

		@Test
		@DisplayName("Should successfully create a new task with valid data")
		void createTask_WithValidData_ShouldReturnCreatedTask() throws IOException {
			// Arrange
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.save(any(Task.class))).thenReturn(testTask);
			when(calendarService.pushToCalendar(any(Task.class))).thenReturn("eventId123");

			// Act
			TaskDto result = taskService.createTask(testTaskDto);

			// Assert
			assertNotNull(result);
			assertEquals(testTaskDto.getTaskName(), result.getTaskName());
			assertEquals(testTaskDto.getPriority(), result.getPriority());
			assertEquals(testTaskDto.getStatus(), result.getStatus());
			verify(userRepository, times(1)).findById(1L);
			verify(taskRepository, times(1)).save(any(Task.class));
			verify(reminderService, times(1)).createReminderForTask(any(Task.class));
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when user not found")
		void createTask_WithNonExistentUser_ShouldThrowException() {
			// Arrange
			when(userRepository.findById(999L)).thenReturn(Optional.empty());
			testTaskDto.setUserId(999L);

			// Act & Assert
			assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(testTaskDto));
			verify(taskRepository, never()).save(any(Task.class));
		}

		@Test
		@DisplayName("Should continue when calendar sync fails during create")
		void createTask_WhenCalendarSyncFails_ShouldStillReturnTask() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.save(any(Task.class))).thenReturn(testTask);

			TaskDto result = taskService.createTask(testTaskDto);

			assertNotNull(result);
			verify(taskRepository, times(1)).save(any(Task.class));
			verify(reminderService, times(1)).createReminderForTask(any(Task.class));
		}
	}

	@Nested
	@DisplayName("getTaskById Tests")
	class GetTaskByIdTests {

		@Test
		@DisplayName("Should return task when user is owner")
		void getTaskById_AsOwner_ShouldReturnTask() {
			// Arrange
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

			// Act
			TaskDto result = taskService.getTaskById(1L, 1L);

			// Assert
			assertNotNull(result);
			assertEquals(testTask.getTaskName(), result.getTaskName());
			verify(taskRepository, times(1)).findById(1L);
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when task not found")
		void getTaskById_WithNonExistentTask_ShouldThrowException() {
			// Arrange
			when(taskRepository.findById(999L)).thenReturn(Optional.empty());

			// Act & Assert
			assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(999L, 1L));
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when user is not owner")
		void getTaskById_AsNonOwner_ShouldThrowException() {
			// Arrange
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

			// Act & Assert
			assertThrows(UnauthorizedAccessException.class, () -> taskService.getTaskById(1L, 999L));
		}
	}

	@Nested
	@DisplayName("getTasksByUserId Tests")
	class GetTasksByUserIdTests {

		@Test
		@DisplayName("Should return all tasks for user")
		void getTasksByUserId_WithValidUser_ShouldReturnTaskList() {
			// Arrange
			List<Task> taskList = Arrays.asList(testTask);
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserId(1L)).thenReturn(taskList);

			// Act
			List<TaskDto> result = taskService.getTasksByUserId(1L);

			// Assert
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(testTask.getTaskName(), result.get(0).getTaskName());
			verify(taskRepository, times(1)).findByUserId(1L);
		}

		@Test
		@DisplayName("Should return empty list when user has no tasks")
		void getTasksByUserId_WithNoTasks_ShouldReturnEmptyList() {
			// Arrange
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserId(1L)).thenReturn(Arrays.asList());

			// Act
			List<TaskDto> result = taskService.getTasksByUserId(1L);

			// Assert
			assertNotNull(result);
			assertTrue(result.isEmpty());
		}

		@Test
		@DisplayName("Should throw ResourceNotFoundException when user not found")
		void getTasksByUserId_WithNonExistentUser_ShouldThrowException() {
			// Arrange
			when(userRepository.findById(999L)).thenReturn(Optional.empty());

			// Act & Assert
			assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByUserId(999L));
			verify(taskRepository, never()).findByUserId(999L);
		}
	}

	@Nested
	@DisplayName("getTasksByPriority Tests")
	class GetTasksByPriorityTests {

		@Test
		@DisplayName("Should return tasks filtered by priority")
		void getTasksByPriority_WithValidPriority_ShouldReturnFilteredTasks() {
			// Arrange
			List<Task> taskList = Arrays.asList(testTask);
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserIdAndPriority(1L, Priority.HIGH)).thenReturn(taskList);

			// Act
			List<TaskDto> result = taskService.getTasksByPriority(1L, Priority.HIGH);

			// Assert
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(Priority.HIGH, result.get(0).getPriority());
		}
	}

	@Nested
	@DisplayName("getTasksByStatus Tests")
	class GetTasksByStatusTests {

		@Test
		@DisplayName("Should return tasks filtered by status")
		void getTasksByStatus_WithValidStatus_ShouldReturnFilteredTasks() {
			// Arrange
			List<Task> taskList = Arrays.asList(testTask);
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserIdAndStatus(1L, Status.PENDING)).thenReturn(taskList);

			// Act
			List<TaskDto> result = taskService.getTasksByStatus(1L, Status.PENDING);

			// Assert
			assertNotNull(result);
			assertEquals(1, result.size());
			assertEquals(Status.PENDING, result.get(0).getStatus());
		}
	}

	@Nested
	@DisplayName("additional filter/update tests")
	class AdditionalCoverageTests {

		@Test
		void getTasksByCompleted_ShouldReturnFilteredTasks() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserIdAndCompleted(1L, false)).thenReturn(List.of(testTask));
			List<TaskDto> result = taskService.getTasksByCompleted(1L, false);
			assertEquals(1, result.size());
			assertFalse(result.get(0).isCompleted());
		}

		@Test
		void getTasksByDateRangeAndDeadline_ShouldReturnFilteredTasks() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserIdAndTaskDeadlineBetween(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
					.thenReturn(List.of(testTask));
			when(taskRepository.findByUserIdAndTaskDeadline(1L, testTask.getTaskDeadline())).thenReturn(List.of(testTask));

			List<TaskDto> range = taskService.getTasksByDateRange(1L, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
			List<TaskDto> deadline = taskService.getTasksByDeadline(1L, testTask.getTaskDeadline());
			assertEquals(1, range.size());
			assertEquals(1, deadline.size());
		}

		@Test
		void getTasksByStatusAndPriority_ShouldReturnFilteredTasks() {
			when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
			when(taskRepository.findByUserIdAndStatusAndPriority(1L, Status.PENDING, Priority.HIGH))
					.thenReturn(List.of(testTask));
			List<TaskDto> result = taskService.getTasksByStatusAndPriority(1L, Status.PENDING, Priority.HIGH);
			assertEquals(1, result.size());
		}

		@Test
		void updateTaskStatusAndPriority_ShouldPersistChanges() {
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
			when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

			TaskDto statusUpdated = taskService.updateTaskStatus(1L, 1L, "IN_PROGRESS");
			TaskDto priorityUpdated = taskService.updateTaskPriority(1L, 1L, Priority.LOW);
			assertEquals(Status.IN_PROGRESS, statusUpdated.getStatus());
			assertEquals(Priority.LOW, priorityUpdated.getPriority());
		}
	}

	@Nested
	@DisplayName("updateTask Tests")
	class UpdateTaskTests {

		@Test
		@DisplayName("Should successfully update task as owner")
		void updateTask_AsOwner_ShouldReturnUpdatedTask() {
			// Arrange
			TaskDto updateDto = TaskDto.builder()
					.taskId(1L)
					.userId(1L)
					.taskName("Updated Task")
					.taskDescription("Updated description")
					.taskDeadline(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
					.priority(Priority.MEDIUM)
					.status(Status.IN_PROGRESS)
					.completed(false)
					.build();

			Task updatedTask = Task.builder()
					.id(testTask.getId())
					.user(testTask.getUser())
					.taskName("Updated Task")
					.taskDescription("Updated description")
					.taskDeadline(LocalDateTime.of(2025, 1, 1, 0, 0, 0))
					.priority(Priority.MEDIUM)
					.status(Status.IN_PROGRESS)
					.completed(testTask.isCompleted())
					.createdAt(testTask.getCreatedAt())
					.updatedAt(testTask.getUpdatedAt())
					.build();

			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
			when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

			// Act
			TaskDto result = taskService.updateTask(1L, 1L, updateDto);

			// Assert
			assertNotNull(result);
			assertEquals("Updated Task", result.getTaskName());
			assertEquals(Priority.MEDIUM, result.getPriority());
			verify(taskRepository, times(1)).save(any(Task.class));
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when user is not owner")
		void updateTask_AsNonOwner_ShouldThrowException() {
			// Arrange
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

			// Act & Assert
			assertThrows(UnauthorizedAccessException.class,
					() -> taskService.updateTask(999L, 1L, testTaskDto));
			verify(taskRepository, never()).save(any(Task.class));
		}

		@Test
		@DisplayName("Should recreate reminder when deadline changes")
		void updateTask_WhenDeadlineChanges_ShouldRecreateReminder() {
			LocalDateTime originalDeadline = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
			LocalDateTime newDeadline = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
			testTask.setTaskDeadline(originalDeadline);
			TaskDto updateDto = TaskDto.builder()
					.taskDeadline(newDeadline)
					.taskName(testTask.getTaskName())
					.taskDescription(testTask.getTaskDescription())
					.priority(testTask.getPriority())
					.status(testTask.getStatus())
					.completed(testTask.isCompleted())
					.build();

			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
			when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

			TaskDto result = taskService.updateTask(1L, 1L, updateDto);

			assertEquals(newDeadline, result.getTaskDeadline());
			verify(reminderService, times(1)).cancelReminderForTask(1L);
			verify(reminderService, times(1)).createReminderForTask(any(Task.class));
		}
	}

	@Nested
	@DisplayName("markTaskAsCompleted Tests")
	class MarkTaskAsCompletedTests {

		@Test
		@DisplayName("Should mark task as completed")
		void markTaskAsCompleted_WithTrue_ShouldUpdateCompletedStatus() {
			// Arrange
			Task completedTask = Task.builder()
					.id(testTask.getId())
					.user(testTask.getUser())
					.taskName(testTask.getTaskName())
					.taskDescription(testTask.getTaskDescription())
					.taskDeadline(testTask.getTaskDeadline())
					.priority(testTask.getPriority())
					.status(Status.COMPLETED)
					.completed(true)
					.createdAt(testTask.getCreatedAt())
					.updatedAt(testTask.getUpdatedAt())
					.build();
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
			when(taskRepository.save(any(Task.class))).thenReturn(completedTask);

			// Act
			TaskDto result = taskService.markTaskAsCompleted(1L, 1L, true);

			// Assert
			assertNotNull(result);
			assertTrue(result.isCompleted());
			assertEquals(Status.COMPLETED, result.getStatus());
		}

		@Test
		@DisplayName("Should mark task as incomplete")
		void markTaskAsCompleted_WithFalse_ShouldUpdateCompletedStatus() {
			// Arrange
			Task incompleteTask = Task.builder()
					.id(testTask.getId())
					.user(testTask.getUser())
					.taskName(testTask.getTaskName())
					.taskDescription(testTask.getTaskDescription())
					.taskDeadline(testTask.getTaskDeadline())
					.priority(testTask.getPriority())
					.status(Status.PENDING)
					.completed(false)
					.createdAt(testTask.getCreatedAt())
					.updatedAt(testTask.getUpdatedAt())
					.build();
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
			when(taskRepository.save(any(Task.class))).thenReturn(incompleteTask);

			// Act
			TaskDto result = taskService.markTaskAsCompleted(1L, 1L, false);

			// Assert
			assertNotNull(result);
			assertFalse(result.isCompleted());
		}
	}

	@Nested
	@DisplayName("deleteTask Tests")
	class DeleteTaskTests {

		@Test
		@DisplayName("Should successfully delete task as owner")
		void deleteTask_AsOwner_ShouldDeleteTask() {
			// Arrange
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
			doNothing().when(reminderService).cancelReminderForTask(1L);

			// Act
			taskService.deleteTask(1L, 1L);

			// Assert
			verify(taskRepository, times(1)).delete(testTask);
			verify(reminderService, times(1)).cancelReminderForTask(1L);
		}

		@Test
		@DisplayName("Should throw UnauthorizedAccessException when user is not owner")
		void deleteTask_AsNonOwner_ShouldThrowException() {
			// Arrange
			when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

			// Act & Assert
			assertThrows(UnauthorizedAccessException.class, () -> taskService.deleteTask(1L, 999L));
			verify(taskRepository, never()).delete(testTask);
		}
	}
}
//commit
