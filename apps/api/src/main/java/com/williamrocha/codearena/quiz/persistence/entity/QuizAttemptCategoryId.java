package com.williamrocha.codearena.quiz.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class QuizAttemptCategoryId implements Serializable {

	@Column(name = "attempt_id")
	private UUID attemptId;

	@Column(name = "category_id")
	private UUID categoryId;

	protected QuizAttemptCategoryId() {
	}

	public QuizAttemptCategoryId(UUID attemptId, UUID categoryId) {
		this.attemptId = attemptId;
		this.categoryId = categoryId;
	}

	public UUID getAttemptId() {
		return attemptId;
	}

	public UUID getCategoryId() {
		return categoryId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof QuizAttemptCategoryId that)) {
			return false;
		}
		return Objects.equals(attemptId, that.attemptId)
			&& Objects.equals(categoryId, that.categoryId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(attemptId, categoryId);
	}
}
