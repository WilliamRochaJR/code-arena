package com.williamrocha.codearena.quiz.attempt.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record QuizAttemptHistoryItemResponse(
	UUID id, QuizAttemptStatus status, Difficulty difficulty,
	List<String> categories, BigDecimal score,
	OffsetDateTime startedAt, OffsetDateTime completedAt
) {
}
