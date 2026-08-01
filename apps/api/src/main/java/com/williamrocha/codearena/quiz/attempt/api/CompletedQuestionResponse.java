package com.williamrocha.codearena.quiz.attempt.api;

import java.util.UUID;

public record CompletedQuestionResponse(
	UUID id,
	short position,
	UUID selectedAlternativeId,
	UUID correctAlternativeId,
	boolean correct,
	String explanation
) {
}
