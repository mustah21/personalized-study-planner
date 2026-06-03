package com.studyplanner.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.studyplanner.backend.dto.ApiResponse;
import com.studyplanner.backend.dto.AuthResponseDto;
import com.studyplanner.backend.dto.ShareTaskDto;
import com.studyplanner.backend.dto.TaskShareInviteDto;
import com.studyplanner.backend.dto.UserLoginDto;
import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.dto.UserSearchdto;
import com.studyplanner.backend.security.JwtUtil;
import com.studyplanner.backend.service.UserService;
import com.studyplanner.backend.util.SecurityUtils;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

        private final UserService userService;
        private final JwtUtil jwtUtil;
        private final UserDetailsService userDetailsService;
        private final SecurityUtils securityUtils;

        // Build user registration
        @PostMapping("/register")
        public ResponseEntity<ApiResponse<AuthResponseDto>> createUser(
                        @RequestBody UserRegisterDto userDto) {

                UserProfileUpdateDto savedUser = userService.createUser(userDto);
                AuthResponseDto authResponse = buildAuthResponse(savedUser);

                ApiResponse<AuthResponseDto> response = ApiResponse.<AuthResponseDto>builder()
                                .status(HttpStatus.CREATED.value())
                                .message("User created successfully")
                                .data(authResponse)
                                .build();

                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        // Build user login
        @PostMapping("/login")
        public ResponseEntity<ApiResponse<AuthResponseDto>> login(
                        @RequestBody UserLoginDto userDto) {

                UserProfileUpdateDto loggedInUser = userService.login(userDto);
                AuthResponseDto authResponse = buildAuthResponse(loggedInUser);

                ApiResponse<AuthResponseDto> response = ApiResponse.<AuthResponseDto>builder()
                                .status(HttpStatus.OK.value())
                                .message("Login successful")
                                .data(authResponse)
                                .build();

                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Build user update
        @PutMapping("/update")
        public ResponseEntity<ApiResponse<UserProfileUpdateDto>> updateProfile(
                        @RequestBody UserProfileUpdateDto userDto) {

                Long userId = securityUtils.getAuthenticatedUserId();
                UserProfileUpdateDto updatedUser = userService.updateProfile(userId, userDto);

                ApiResponse<UserProfileUpdateDto> response = ApiResponse.<UserProfileUpdateDto>builder()
                                .status(HttpStatus.OK.value())
                                .message("Profile updated successfully")
                                .data(updatedUser)
                                .build();

                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // Build get user by id
        @GetMapping("/me")
        public ResponseEntity<ApiResponse<UserProfileUpdateDto>> getUserById() {

                Long userId = securityUtils.getAuthenticatedUserId();
                UserProfileUpdateDto user = userService.getUserById(userId);

                ApiResponse<UserProfileUpdateDto> response = ApiResponse.<UserProfileUpdateDto>builder()
                                .status(HttpStatus.OK.value())
                                .message("User found successfully")
                                .data(user)
                                .build();

                return new ResponseEntity<>(response, HttpStatus.OK);
        }

        // call search on every keystroke from the frontend
        @GetMapping("/search")
        public ResponseEntity<ApiResponse<List<UserSearchdto>>> searchUsers(
                        @RequestParam String query) {

                Long excludeUserId = securityUtils.getAuthenticatedUserId();
                List<UserSearchdto> results = userService.searchUsers(query, excludeUserId);
                return ResponseEntity.ok(
                                ApiResponse.<List<UserSearchdto>>builder()
                                                .status(HttpStatus.OK.value())
                                                .message(results.isEmpty()
                                                                ? "No users found matching: " + query
                                                                : results.size() + " user(s) found")
                                                .data(results)
                                                .build());
        }

        // share one or multiple tasks
        @PostMapping("/share")
        public ResponseEntity<ApiResponse<List<TaskShareInviteDto>>> shareTasks(
                        @Valid @RequestBody ShareTaskDto shareTaskDto) {

                // Inject sender from token — client cannot spoof this
                shareTaskDto.setSenderUserId(securityUtils.getAuthenticatedUserId());

                List<TaskShareInviteDto> invites = userService.shareTasks(shareTaskDto);
                return ResponseEntity.status(HttpStatus.CREATED).body(
                                ApiResponse.<List<TaskShareInviteDto>>builder()
                                                .status(HttpStatus.CREATED.value())
                                                .message(invites.size() + " invite(s) sent successfully")
                                                .data(invites)
                                                .build());
        }

        @GetMapping("/invites/accept")
        public ResponseEntity<ApiResponse<TaskShareInviteDto>> acceptViaEmail(
                        @RequestParam String token) {

                return ResponseEntity.ok(
                                ApiResponse.<TaskShareInviteDto>builder()
                                                .status(HttpStatus.OK.value())
                                                .message("Task accepted and added to your calendar!")
                                                .data(userService.acceptInvite(token))
                                                .build());
        }

        @GetMapping("/invites/decline")
        public ResponseEntity<ApiResponse<TaskShareInviteDto>> declineViaEmail(
                        @RequestParam String token) {

                return ResponseEntity.ok(
                                ApiResponse.<TaskShareInviteDto>builder()
                                                .status(HttpStatus.OK.value())
                                                .message("Task invite declined.")
                                                .data(userService.declineInvite(token))
                                                .build());
        }

        @PatchMapping("/invites/accept")
        public ResponseEntity<ApiResponse<TaskShareInviteDto>> acceptInApp(
                        @RequestParam String token) {

                return ResponseEntity.ok(
                                ApiResponse.<TaskShareInviteDto>builder()
                                                .status(HttpStatus.OK.value())
                                                .message("Task accepted and added to your calendar!")
                                                .data(userService.acceptInvite(token))
                                                .build());
        }

        @PatchMapping("/invites/decline")
        public ResponseEntity<ApiResponse<TaskShareInviteDto>> declineInApp(
                        @RequestParam String token) {

                return ResponseEntity.ok(
                                ApiResponse.<TaskShareInviteDto>builder()
                                                .status(HttpStatus.OK.value())
                                                .message("Task invite declined.")
                                                .data(userService.declineInvite(token))
                                                .build());
        }

        @GetMapping("/invites/pending")
        public ResponseEntity<ApiResponse<List<TaskShareInviteDto>>> getPendingInvites() {
                Long userId = securityUtils.getAuthenticatedUserId();
                List<TaskShareInviteDto> pending = userService.getPendingInvites(userId);
                return ResponseEntity.ok(
                                ApiResponse.<List<TaskShareInviteDto>>builder()
                                                .status(HttpStatus.OK.value())
                                                .message(pending.size() + " pending invite(s)")
                                                .data(pending)
                                                .build());
        }

        @GetMapping("/invites")
        public ResponseEntity<ApiResponse<List<TaskShareInviteDto>>> getAllReceivedInvites() {
                Long userId = securityUtils.getAuthenticatedUserId();
                List<TaskShareInviteDto> all = userService.getAllReceivedInvites(userId);
                return ResponseEntity.ok(
                                ApiResponse.<List<TaskShareInviteDto>>builder()
                                                .status(HttpStatus.OK.value())
                                                .message(all.size() + " invite(s) found")
                                                .data(all)
                                                .build());
        }

        @GetMapping("/invites/count")
        public ResponseEntity<ApiResponse<Long>> countPendingInvites() {
                return ResponseEntity.ok(
                                ApiResponse.<Long>builder()
                                                .status(HttpStatus.OK.value())
                                                .message("Pending invite count")
                                                .data(userService.countPendingInvites(
                                                                securityUtils.getAuthenticatedUserId()))
                                                .build());
        }

        private AuthResponseDto buildAuthResponse(UserProfileUpdateDto user) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
                String token = jwtUtil.generateToken(Map.of("userId", user.getUserId()), userDetails);
                return AuthResponseDto.builder()
                                .token(token)
                                .user(user)
                                .build();
        }
}
