package com.williamrocha.codearena.quiz.persistence.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "question_categories")
public class QuestionCategory {

	@EmbeddedId
	private QuestionCategoryId id;

	@MapsId("questionId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@MapsId("categoryId")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	protected QuestionCategory() {
	}

	public QuestionCategory(Question question, Category category) {
		this.id = new QuestionCategoryId(question.getId(), category.getId());
		this.question = question;
		this.category = category;
	}

	public QuestionCategoryId getId() {
		return id;
	}

	public Question getQuestion() {
		return question;
	}

	public Category getCategory() {
		return category;
	}
}
