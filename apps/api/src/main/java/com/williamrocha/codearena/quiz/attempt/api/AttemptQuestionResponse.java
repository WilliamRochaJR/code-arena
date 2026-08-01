package com.williamrocha.codearena.quiz.attempt.api;

import java.util.List;
import java.util.UUID;

public record AttemptQuestionResponse(
	UUID id,
	short position,
	String statement,
	List<String> categories,
	List<AlternativeResponse> alternatives,
	UUID selectedAlternativeId
) {
}
