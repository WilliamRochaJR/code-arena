package com.williamrocha.codearena.quiz.attempt.application;

import java.util.List;

public record QuizAttemptHistory(
	List<QuizAttemptHistoryItem> items,
	int page,
	int size,
	long totalItems,
	int totalPages
) {
}
