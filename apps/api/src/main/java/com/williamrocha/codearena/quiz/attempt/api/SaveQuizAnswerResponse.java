package com.williamrocha.codearena.quiz.attempt.api;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SaveQuizAnswerResponse(
	UUID questionId,
	UUID selectedAlternativeId,
	OffsetDateTime answeredAt
) {
}
