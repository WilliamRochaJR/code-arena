package com.williamrocha.codearena.quiz.attempt.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record CreateQuizAttemptResponse(
	UUID id,
	QuizAttemptStatus status,
	Difficulty difficulty,
	List<String> categories,
	short totalQuestions,
	short answeredQuestions,
	OffsetDateTime startedAt
) {

	public CreateQuizAttemptResponse {
		categories = List.copyOf(categories);
	}
}
