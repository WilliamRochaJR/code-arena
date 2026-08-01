package com.williamrocha.codearena.quiz.attempt.application;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record QuizAttemptDetails(
	UUID id,
	QuizAttemptStatus status,
	Difficulty difficulty,
	short totalQuestions,
	short answeredQuestions,
	OffsetDateTime startedAt,
	List<AttemptQuestionDetails> questions
) {
}
