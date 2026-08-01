package com.williamrocha.codearena.quiz.attempt.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.attempt.application.CompletedQuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record CompleteQuizAttemptResponse(
	UUID attemptId,
	QuizAttemptStatus status,
	short totalQuestions,
	short correctAnswers,
	BigDecimal score,
	OffsetDateTime startedAt,
	OffsetDateTime completedAt,
	List<CategoryPerformanceResponse> performanceByCategory,
	List<CompletedQuestionResponse> questions
) {

	static CompleteQuizAttemptResponse from(CompletedQuizAttempt completed) {
		return new CompleteQuizAttemptResponse(
			completed.attemptId(), completed.status(), completed.totalQuestions(),
			completed.correctAnswers(), completed.score(), completed.startedAt(),
			completed.completedAt(),
			completed.performanceByCategory().stream()
				.map(item -> new CategoryPerformanceResponse(
					item.category(), item.correct(), item.total()))
				.toList(),
			completed.questions().stream()
				.map(item -> new CompletedQuestionResponse(
					item.id(), item.position(), item.selectedAlternativeId(),
					item.correctAlternativeId(), item.correct(), item.explanation()))
				.toList());
	}
}
