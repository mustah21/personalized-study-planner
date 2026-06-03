package com.studyplanner.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyplanner.backend.dto.ShareTaskDto;
import com.studyplanner.backend.dto.TaskShareInviteDto;
import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.dto.UserSearchdto;
import com.studyplanner.backend.entity.TaskShareInvite.InviteStatus;
import com.studyplanner.backend.security.JwtUtil;
import com.studyplanner.backend.service.UserService;
import com.studyplanner.backend.util.SecurityUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Tests")
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserDetailsService userDetailsService;

    private TestSecurityUtils testSecurityUtils;

    private UserController userController;

    @BeforeEach
    void setUp() {
        testSecurityUtils = new TestSecurityUtils();
        userController = new UserController(userService, jwtUtil, userDetailsService, testSecurityUtils);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new TestExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void register_ShouldReturnAuthPayload() throws Exception {
        UserRegisterDto request = new UserRegisterDto("student@example.com", "password123");
        UserProfileUpdateDto savedUser = UserProfileUpdateDto.builder()
                .userId(10L)
                .email("student@example.com")
                .firstName("Student")
                .build();

        when(userService.createUser(any(UserRegisterDto.class))).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername("student@example.com"))
                .thenReturn(User.withUsername("student@example.com").password("x").authorities("USER").build());
        when(jwtUtil.generateToken(anyMap(), any())).thenReturn("jwt-token");

        mockMvc.perform(post("/api/v1/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.userId").value(10L));
    }

    @Test
    void searchUsers_ShouldExcludeAuthenticatedUser() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(42L);
        when(userService.searchUsers("ri", 42L)).thenReturn(List.of(
                UserSearchdto.builder().userId(2L).firstName("Rita").email("rita@example.com").build()));

        mockMvc.perform(get("/api/v1/users/search").param("query", "ri"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data[0].firstName").value("Rita"));

        verify(userService, times(1)).searchUsers("ri", 42L);
    }

    @Test
    void shareTasks_ShouldInjectSenderAndReturnCreated() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(1L);
        ShareTaskDto request = ShareTaskDto.builder()
                .receiverUserIds(List.of(2L))
                .taskIds(List.of(101L))
                .build();
        TaskShareInviteDto invite = TaskShareInviteDto.builder()
                .inviteId(11L)
                .status(InviteStatus.PENDING)
                .taskId(101L)
                .taskName("Math revision")
                .createdAt(LocalDateTime.now())
                .build();
        when(userService.shareTasks(any(ShareTaskDto.class))).thenReturn(List.of(invite));

        mockMvc.perform(post("/api/v1/users/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("1 invite(s) sent successfully"))
                .andExpect(jsonPath("$.data[0].taskId").value(101L));

        verify(userService, times(1)).shareTasks(any(ShareTaskDto.class));
    }

    @Test
    void acceptInviteViaPatch_ShouldReturnOk() throws Exception {
        TaskShareInviteDto accepted = TaskShareInviteDto.builder()
                .inviteId(11L)
                .status(InviteStatus.ACCEPTED)
                .build();
        when(userService.acceptInvite("abc-token")).thenReturn(accepted);

        mockMvc.perform(patch("/api/v1/users/invites/accept").param("token", "abc-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.message").value("Task accepted and added to your calendar!"));
    }

    @Test
    void declineInviteViaGet_ShouldReturnOk() throws Exception {
        TaskShareInviteDto declined = TaskShareInviteDto.builder()
                .inviteId(11L)
                .status(InviteStatus.DECLINED)
                .build();
        when(userService.declineInvite("abc-token")).thenReturn(declined);

        mockMvc.perform(get("/api/v1/users/invites/decline").param("token", "abc-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DECLINED"));
    }

    @Test
    void pendingAndCountEndpoints_ShouldReturnInviteStats() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(2L);
        when(userService.getPendingInvites(2L)).thenReturn(List.of(TaskShareInviteDto.builder()
                .inviteId(77L)
                .status(InviteStatus.PENDING)
                .build()));
        when(userService.countPendingInvites(2L)).thenReturn(1L);

        mockMvc.perform(get("/api/v1/users/invites/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").value("PENDING"));

        mockMvc.perform(get("/api/v1/users/invites/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    void shareTasks_WithServiceValidationError_ShouldReturnBadRequest() throws Exception {
        testSecurityUtils.setAuthenticatedUserId(1L);
        ShareTaskDto request = ShareTaskDto.builder()
                .receiverUserIds(List.of(1L))
                .taskIds(List.of(101L))
                .build();
        when(userService.shareTasks(any(ShareTaskDto.class)))
                .thenThrow(new IllegalArgumentException("You cannot share tasks with yourself"));

        mockMvc.perform(post("/api/v1/users/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    static class TestSecurityUtils extends SecurityUtils {
        private Long id;

        TestSecurityUtils() {
            super(null);
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
        @ExceptionHandler(IllegalArgumentException.class)
        ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
