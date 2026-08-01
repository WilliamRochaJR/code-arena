package com.williamrocha.codearena.quiz.attempt.application;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record CompletedQuizAttempt(
	UUID attemptId,
	QuizAttemptStatus status,
	short totalQuestions,
	short correctAnswers,
	BigDecimal score,
	OffsetDateTime startedAt,
	OffsetDateTime completedAt,
	List<CategoryPerformance> performanceByCategory,
	List<CompletedQuestion> questions
) {
}
