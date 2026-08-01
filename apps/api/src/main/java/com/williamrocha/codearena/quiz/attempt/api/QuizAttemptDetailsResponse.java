package com.williamrocha.codearena.quiz.attempt.api;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptDetails;
import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public record QuizAttemptDetailsResponse(
	UUID id,
	QuizAttemptStatus status,
	Difficulty difficulty,
	short totalQuestions,
	short answeredQuestions,
	OffsetDateTime startedAt,
	List<AttemptQuestionResponse> questions
) {

	static QuizAttemptDetailsResponse from(QuizAttemptDetails details) {
		return new QuizAttemptDetailsResponse(
			details.id(),
			details.status(),
			details.difficulty(),
			details.totalQuestions(),
			details.answeredQuestions(),
			details.startedAt(),
			details.questions().stream()
				.map(question -> new AttemptQuestionResponse(
					question.id(),
					question.position(),
					question.statement(),
					question.categories(),
					question.alternatives().stream()
						.map(alternative -> new AlternativeResponse(
							alternative.id(),
							alternative.text()))
						.toList(),
					question.selectedAlternativeId()))
				.toList());
	}
}
