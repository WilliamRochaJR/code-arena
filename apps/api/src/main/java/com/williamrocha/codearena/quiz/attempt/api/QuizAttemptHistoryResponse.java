package com.williamrocha.codearena.quiz.attempt.api;

import java.util.List;

import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptHistory;

public record QuizAttemptHistoryResponse(
	List<QuizAttemptHistoryItemResponse> items,
	int page,
	int size,
	long totalItems,
	int totalPages
) {

	static QuizAttemptHistoryResponse from(QuizAttemptHistory history) {
		return new QuizAttemptHistoryResponse(
			history.items().stream()
				.map(item -> new QuizAttemptHistoryItemResponse(
					item.id(), item.status(), item.difficulty(), item.categories(),
					item.score(), item.startedAt(), item.completedAt()))
				.toList(),
			history.page(), history.size(), history.totalItems(), history.totalPages());
	}
}
