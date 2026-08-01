package com.williamrocha.codearena.quiz.attempt.application;

import java.util.List;
import java.util.UUID;

public record AttemptQuestionDetails(
	UUID id,
	short position,
	String statement,
	List<String> categories,
	List<AlternativeDetails> alternatives,
	UUID selectedAlternativeId
) {
}
