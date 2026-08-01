package com.williamrocha.codearena.quiz.attempt.api;

public record CategoryPerformanceResponse(
	String category,
	short correct,
	short total
) {
}
