package com.williamrocha.codearena.quiz.attempt.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SaveQuizAnswerRequest(
	@NotNull(message = "selecione uma alternativa")
	UUID selectedAlternativeId
) {
}
