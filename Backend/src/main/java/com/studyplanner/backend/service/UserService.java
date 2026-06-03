package com.studyplanner.backend.service;

import java.util.List;

import com.studyplanner.backend.dto.ShareTaskDto;
import com.studyplanner.backend.dto.TaskShareInviteDto;
import com.studyplanner.backend.dto.UserLoginDto;
import com.studyplanner.backend.dto.UserProfileUpdateDto;
import com.studyplanner.backend.dto.UserRegisterDto;
import com.studyplanner.backend.dto.UserSearchdto;

public interface UserService {

    UserProfileUpdateDto createUser(UserRegisterDto userDto);

    UserProfileUpdateDto login(UserLoginDto userDto);

    UserProfileUpdateDto updateProfile(Long userId, UserProfileUpdateDto userDto);

    UserProfileUpdateDto getUserById(Long userId);

    // user search based on name or email
    List<UserSearchdto> searchUsers(String query, Long excludeUserId);

    // Share one or more tasks with another user
    List<TaskShareInviteDto> shareTasks(ShareTaskDto shareTaskDto);

    // Accept a task share invite via invite token from email or in-app notification
    TaskShareInviteDto acceptInvite(String inviteToken);

    // Decline a task share invite via inviteToken email or in-app notification
    TaskShareInviteDto declineInvite(String inviteToken);

    // get all pending task share invites for a user (used for in-app notifications)
    List<TaskShareInviteDto> getPendingInvites(Long userId);

    // Get full history
    List<TaskShareInviteDto> getAllReceivedInvites(Long userId);

    // count unread invites for in-app notification badge
    long countPendingInvites(Long userId);

}
