package com.williamrocha.codearena.quiz.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_attempt_categories")
public class QuizAttemptCategory {

	@EmbeddedId
	private QuizAttemptCategoryId id;

	@MapsId("attemptId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attempt_id", nullable = false)
	private QuizAttempt attempt;

	@MapsId("categoryId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	protected QuizAttemptCategory() {
	}

	public QuizAttemptCategory(QuizAttempt attempt, Category category) {
		this.id = new QuizAttemptCategoryId(attempt.getId(), category.getId());
		this.attempt = attempt;
		this.category = category;
	}

	public QuizAttemptCategoryId getId() {
		return id;
	}

	public QuizAttempt getAttempt() {
		return attempt;
	}

	public Category getCategory() {
		return category;
	}
}
