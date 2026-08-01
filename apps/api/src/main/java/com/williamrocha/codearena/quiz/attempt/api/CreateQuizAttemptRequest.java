package com.williamrocha.codearena.quiz.attempt.api;

import java.util.List;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record CreateQuizAttemptRequest(
	@NotNull(message = "informe a dificuldade") Difficulty difficulty,
	@NotEmpty(message = "selecione ao menos uma categoria")
	List<@NotBlank(message = "informe uma categoria valida") String> categories
) {

	public CreateQuizAttemptRequest {
		categories = categories == null
			? null
			: categories.stream().toList();
	}
}
