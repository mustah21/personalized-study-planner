package com.studyplanner.backend.repository;

import com.studyplanner.backend.entity.SuggestedLLM;
import com.studyplanner.backend.entity.SuggestedLLM.SuggestedStatus;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuggestedTaskRepository extends JpaRepository<SuggestedLLM, Long> {

    // Get all suggestions for a user
    List<SuggestedLLM> findByUserId(Long userId);

    // Get only pending suggestions for a user
    List<SuggestedLLM> findByUserIdAndSuggestedStatus(Long userId, SuggestedStatus suggestedStatus);

    // ------------------ This is for analytics ------------------
    // ------------------ Currently not in use (created for future use
    // cases)------------------
    // Count suggestions by a user and status
    long countByUserIdAndSuggestedStatus(Long userId, SuggestedStatus suggestedStatus);

    // count all suggestions for a user in a date range, for rate limit
    @Query("SELECT count(s) FROM SuggestedLLM s " +
            "WHERE s.user.id = :userId " +
            "AND s.taskDeadline BETWEEN :start AND :end")
    long countByUserIdAndTaskDeadlineBetween(
            @Param("userId") Long userId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Analytics: acceptance rate for a user
    @Query("SELECT " +
            "CAST(COUNT(CASE WHEN s.suggestedStatus = 'ACCEPTED' THEN 1 END) as double) / COUNT(*) " +
            " FROM SuggestedLLM s " +
            "WHERE s.user.id = :userId")
    double getAcceptanceRateByUserId(@Param("userId") Long userId);

    // Get suggestions created in the last N days for a user
    @Query("SELECT s FROM SuggestedLLM s " +
            "WHERE s.user.id = :userId " +
            "AND s.createdAt >= :since " +
            "ORDER BY s.createdAt DESC")
    List<SuggestedLLM> findRecentSuggestionsByUserId(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);

}