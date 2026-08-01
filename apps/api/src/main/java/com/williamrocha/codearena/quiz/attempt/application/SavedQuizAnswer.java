package com.williamrocha.codearena.quiz.attempt.application;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SavedQuizAnswer(
	UUID questionId,
	UUID selectedAlternativeId,
	OffsetDateTime answeredAt
) {
}
