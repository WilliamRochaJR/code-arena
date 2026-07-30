package com.williamrocha.codearena.quiz.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
	name = "alternatives",
	uniqueConstraints = {
		@UniqueConstraint(
			name = "uk_alternatives_question_id",
			columnNames = {"question_id", "id"}
		)
	}
)
public class Alternative {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Column(nullable = false, columnDefinition = "text")
	private String text;

	@Column(nullable = false)
	private boolean correct;

	@Column(name = "display_order", nullable = false)
	private short displayOrder;

	protected Alternative() {
	}

	public Alternative(
		Question question,
		String text,
		boolean correct,
		short displayOrder
	) {
		this.id = UUID.randomUUID();
		this.question = question;
		this.text = text;
		this.correct = correct;
		this.displayOrder = displayOrder;
	}

	public UUID getId() {
		return id;
	}

	public Question getQuestion() {
		return question;
	}

	public String getText() {
		return text;
	}

	public boolean isCorrect() {
		return correct;
	}

	public short getDisplayOrder() {
		return displayOrder;
	}
}
