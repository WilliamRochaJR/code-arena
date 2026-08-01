package com.williamrocha.codearena.quiz.attempt.application;

import java.util.UUID;

public record CompletedQuestion(
	UUID id,
	short position,
	UUID selectedAlternativeId,
	UUID correctAlternativeId,
	boolean correct,
	String explanation
) {
}
