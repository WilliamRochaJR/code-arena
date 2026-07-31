package com.williamrocha.codearena.quiz.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class QuestionCategoryId implements Serializable {

	@Column(name = "question_id")
	private UUID questionId;

	@Column(name = "category_id")
	private UUID categoryId;

	protected QuestionCategoryId() {
	}

	public QuestionCategoryId(UUID questionId, UUID categoryId) {
		this.questionId = questionId;
		this.categoryId = categoryId;
	}

	public UUID getQuestionId() {
		return questionId;
	}

	public UUID getCategoryId() {
		return categoryId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof QuestionCategoryId that)) {
			return false;
		}
		return Objects.equals(questionId, that.questionId)
			&& Objects.equals(categoryId, that.categoryId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(questionId, categoryId);
	}
}
