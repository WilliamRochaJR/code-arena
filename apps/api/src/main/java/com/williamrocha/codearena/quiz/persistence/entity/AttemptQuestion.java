package com.williamrocha.codearena.quiz.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "attempt_questions")
public class AttemptQuestion {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attempt_id", nullable = false)
	private QuizAttempt attempt;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "question_id", nullable = false)
	private Question question;

	@Column(nullable = false)
	private short position;

	@Column(name = "selected_alternative_id")
	private UUID selectedAlternativeId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
		@JoinColumn(
			name = "question_id",
			referencedColumnName = "question_id",
			insertable = false,
			updatable = false
		),
		@JoinColumn(
			name = "selected_alternative_id",
			referencedColumnName = "id",
			insertable = false,
			updatable = false
		)
	})
	private Alternative selectedAlternative;

	private Boolean correct;

	@Column(name = "answered_at")
	private OffsetDateTime answeredAt;

	protected AttemptQuestion() {
	}

	public AttemptQuestion(QuizAttempt attempt, Question question, short position) {
		this.id = UUID.randomUUID();
		this.attempt = attempt;
		this.question = question;
		this.position = position;
	}

	public UUID getId() {
		return id;
	}

	public QuizAttempt getAttempt() {
		return attempt;
	}

	public Question getQuestion() {
		return question;
	}

	public short getPosition() {
		return position;
	}

	public Alternative getSelectedAlternative() {
		return selectedAlternative;
	}

	public UUID getSelectedAlternativeId() {
		return selectedAlternativeId;
	}

	public Boolean getCorrect() {
		return correct;
	}

	public OffsetDateTime getAnsweredAt() {
		return answeredAt;
	}

	public void answerWith(UUID alternativeId, OffsetDateTime answeredAt) {
		if (alternativeId.equals(selectedAlternativeId)) {
			return;
		}

		this.selectedAlternativeId = alternativeId;
		this.selectedAlternative = null;
		this.answeredAt = answeredAt;
	}

	public void markCorrect(boolean correct) {
		this.correct = correct;
	}
}
