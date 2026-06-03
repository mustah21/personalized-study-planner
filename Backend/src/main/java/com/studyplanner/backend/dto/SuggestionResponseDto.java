package com.studyplanner.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SuggestionResponseDto {

    // What the user wants to do with each suggestion
    private List<Long> acceptedIds; // e.g., [9, 10, 11]
    private List<Long> declinedIds; // e.g., [12, 13]
}