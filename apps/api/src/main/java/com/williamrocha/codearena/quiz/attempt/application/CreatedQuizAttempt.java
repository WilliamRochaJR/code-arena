package com.williamrocha.codearena.quiz.attempt.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record CreatedQuizAttempt(
	UUID id,
	QuizAttemptStatus status,
	Difficulty difficulty,
	List<String> categories,
	short totalQuestions,
	OffsetDateTime startedAt
) {

	public CreatedQuizAttempt {
		categories = List.copyOf(categories);
	}
}
