package com.studyplanner.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchdto {

    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;

}
