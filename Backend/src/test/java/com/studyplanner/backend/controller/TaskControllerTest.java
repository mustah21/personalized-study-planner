package com.studyplanner.backend.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyplanner.backend.dto.TaskDto;
import com.studyplanner.backend.entity.Task.Priority;
import com.studyplanner.backend.entity.Task.Status;
import com.studyplanner.backend.exception.ResourceNotFoundException;
import com.studyplanner.backend.exception.UnauthorizedAccessException;
import com.studyplanner.backend.service.TaskService;
import com.studyplanner.backend.util.SecurityUtils;

/**
 * Unit tests for TaskController using standalone MockMvc and Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TaskController Tests")
class TaskControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private TaskService taskService;

    // replace mocking SecurityUtils (ByteBuddy/Java 25 issue) with a small test double
    private TestSecurityUtils testSecurityUtils;

    private TaskController taskController;

    private TaskDto testTaskDto;

    @BeforeEach
    void setUp() {
        testSecurityUtils = new TestSecurityUtils();
        taskController = new TaskController(taskService, testSecurityUtils);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(taskController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(validator)
                .build();

        testTaskDto = TaskDto.builder()
                .taskId(1L)
                .userId(1L)
                .taskName("Test Task")
                .taskDescription("A test task")
                .taskDeadline(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                .priority(Priority.HIGH)
                .status(Status.PENDING)
                .completed(false)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/task/create Tests")
    class CreateTaskTests {

        @Test
        @DisplayName("Should create a task successfully with valid data")
        void createTask_WithValidData_ShouldReturnCreatedTask() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.createTask(any(TaskDto.class))).thenReturn(testTaskDto);

            // Act & Assert
            mockMvc.perform(post("/api/v1/task/create")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testTaskDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("Task created successfully"))
                    .andExpect(jsonPath("$.data.taskName").value("Test Task"));

            verify(taskService, times(1)).createTask(any(TaskDto.class));
        }


    }

    @Nested
    @DisplayName("GET /api/v1/task/{taskId} Tests")
    class GetTaskByIdTests {

        @Test
        @DisplayName("Should return task by ID for authenticated user")
        void getTaskById_WithValidTaskId_ShouldReturnTask() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTaskById(1L, 1L)).thenReturn(testTaskDto);

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data.taskName").value("Test Task"));

            verify(taskService, times(1)).getTaskById(1L, 1L);
        }

        @Test
        @DisplayName("Should return 404 when task not found")
        void getTaskById_WithNonExistentTaskId_ShouldReturn404() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTaskById(999L, 1L))
                    .thenThrow(new ResourceNotFoundException("Task not Found"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when user is not owner")
        void getTaskById_AsNonOwner_ShouldReturn403() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(999L);
            when(taskService.getTaskById(1L, 999L))
                    .thenThrow(new UnauthorizedAccessException("Access Denied"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/task/user Tests")
    class GetAllTasksByUserTests {

        @Test
        @DisplayName("Should return all tasks for authenticated user")
        void getAllTasksByUser_ShouldReturnTaskList() throws Exception {
            // Arrange
            List<TaskDto> taskList = List.of(testTaskDto);
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTasksByUserId(1L)).thenReturn(taskList);

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/user")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.data[0].taskName").value("Test Task"))
                    .andExpect(jsonPath("$.data.length()").value(1));

            verify(taskService, times(1)).getTasksByUserId(1L);
        }

        @Test
        @DisplayName("Should return empty list when user has no tasks")
        void getAllTasksByUser_WithNoTasks_ShouldReturnEmptyList() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTasksByUserId(1L)).thenReturn(List.of());

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/user")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/task/user/filter Tests")
    class FilterTasksTests {

        @Test
        @DisplayName("Should filter tasks by priority")
        void filterTasks_ByPriority_ShouldReturnFilteredTasks() throws Exception {
            // Arrange
            List<TaskDto> filteredTasks = List.of(testTaskDto);
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTasksByPriority(1L, Priority.HIGH)).thenReturn(filteredTasks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/user/filter")
                    .param("priority", "HIGH")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].priority").value("HIGH"));

            verify(taskService, times(1)).getTasksByPriority(1L, Priority.HIGH);
        }

        @Test
        @DisplayName("Should filter tasks by status")
        void filterTasks_ByStatus_ShouldReturnFilteredTasks() throws Exception {
            // Arrange
            List<TaskDto> filteredTasks = List.of(testTaskDto);
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTasksByStatus(1L, Status.PENDING)).thenReturn(filteredTasks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/user/filter")
                    .param("status", "PENDING")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("Should filter tasks by completed status")
        void filterTasks_ByCompleted_ShouldReturnFilteredTasks() throws Exception {
            // Arrange
            List<TaskDto> filteredTasks = List.of(testTaskDto);
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTasksByCompleted(1L, false)).thenReturn(filteredTasks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/user/filter")
                    .param("completed", "false")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should filter tasks by priority and status")
        void filterTasks_ByPriorityAndStatus_ShouldReturnFilteredTasks() throws Exception {
            // Arrange
            List<TaskDto> filteredTasks = List.of(testTaskDto);
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.getTasksByStatusAndPriority(1L, Status.PENDING, Priority.HIGH))
                    .thenReturn(filteredTasks);

            // Act & Assert
            mockMvc.perform(get("/api/v1/task/user/filter")
                    .param("priority", "HIGH")
                    .param("status", "PENDING")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].priority").value("HIGH"))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/task/update/{taskId} Tests")
    class UpdateTaskTests {

        @Test
        @DisplayName("Should update task successfully")
        void updateTask_WithValidData_ShouldReturnUpdatedTask() throws Exception {
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
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.updateTask(eq(1L), eq(1L), any(TaskDto.class))).thenReturn(updateDto);

            // Act & Assert
            mockMvc.perform(put("/api/v1/task/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task updated successfully"))
                    .andExpect(jsonPath("$.data.taskName").value("Updated Task"));

            verify(taskService, times(1)).updateTask(eq(1L), eq(1L), any(TaskDto.class));
        }

        @Test
        @DisplayName("Should return 403 when user is not owner")
        void updateTask_AsNonOwner_ShouldReturn403() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(999L);
            // Note: controller calls updateTask(userId=999L, taskId=1L, taskDto)
            when(taskService.updateTask(eq(999L), eq(1L), any(TaskDto.class)))
                    .thenThrow(new UnauthorizedAccessException("Access Denied"));

            // Act & Assert
            mockMvc.perform(put("/api/v1/task/update/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testTaskDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/task/update/{taskId}/complete Tests")
    class MarkCompletedTests {

        @Test
        @DisplayName("Should mark task as completed")
        void markCompleted_WithTrue_ShouldReturnCompletedTask() throws Exception {
            // Arrange
            TaskDto completedTask = TaskDto.builder()
                    .taskId(1L)
                    .userId(1L)
                    .taskName("Test Task")
                    .taskDescription("A test task")
                    .taskDeadline(LocalDateTime.of(2024, 12, 31, 23, 59, 59))
                    .priority(Priority.HIGH)
                    .status(Status.COMPLETED)
                    .completed(true)
                    .build();
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.markTaskAsCompleted(1L, 1L, true)).thenReturn(completedTask);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/task/update/1/complete")
                    .param("completed", "true")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task marked as completed"));

            verify(taskService, times(1)).markTaskAsCompleted(1L, 1L, true);
        }

        @Test
        @DisplayName("Should mark task as incomplete")
        void markCompleted_WithFalse_ShouldReturnIncompleteTask() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(1L);
            when(taskService.markTaskAsCompleted(1L, 1L, false)).thenReturn(testTaskDto);

            // Act & Assert
            mockMvc.perform(patch("/api/v1/task/update/1/complete")
                    .param("completed", "false")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task marked as incomplete"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/task/{taskId} Tests")
    class DeleteTaskTests {

        @Test
        @DisplayName("Should delete task successfully")
        void deleteTask_WithValidTaskId_ShouldDelete() throws Exception {
            // Arrange
            testSecurityUtils.setAuthenticatedUserId(1L);
            // Controller calls deleteTask(taskId=1L, userId=1L)
            doNothing().when(taskService).deleteTask(1L, 1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/task/delete/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Task deleted successfully"));

            verify(taskService, times(1)).deleteTask(1L, 1L);
        }

//        @Test
//        @DisplayName("Should return 403 when user is not owner")
//        void deleteTask_AsNonOwner_ShouldReturn403() throws Exception {
//            // Arrange
//            testSecurityUtils.setAuthenticatedUserId(999L);
//            // Controller calls deleteTask(taskId=1L, userId=999L)
//            doThrow(new UnauthorizedAccessException("Access Denied"))
//                    .when(taskService).deleteTask(1L, 999L);
//
//            // Act & Assert
//            mockMvc.perform(delete("/api/v1/task/delete/1")
//                    .contentType(MediaType.APPLICATION_JSON))
//                    .andExpect(status().isForbidden());
//        }
    }

    // lightweight test double for SecurityUtils used in tests
    static class TestSecurityUtils extends SecurityUtils {
        private Long id;

        TestSecurityUtils() {
            super(null); // UserRepository is not used by tests
        }

        void setAuthenticatedUserId(Long id) {
            this.id = id;
        }

        @Override
        public Long getAuthenticatedUserId() {
            return id;
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
        }

        @ExceptionHandler(UnauthorizedAccessException.class)
        ResponseEntity<String> handleUnauthorized(UnauthorizedAccessException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
        }
    }

}
