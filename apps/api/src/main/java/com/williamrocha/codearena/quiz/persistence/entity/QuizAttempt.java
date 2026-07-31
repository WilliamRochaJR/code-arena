package com.williamrocha.codearena.quiz.persistence.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {

	public static final short TOTAL_QUESTIONS = 10;

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private AppUser user;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Difficulty difficulty;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private QuizAttemptStatus status;

	@Column(name = "total_questions", nullable = false)
	private short totalQuestions;

	@Column(name = "correct_answers")
	private Short correctAnswers;

	@Column(precision = 5, scale = 2)
	private BigDecimal score;

	@Column(name = "started_at", nullable = false)
	private OffsetDateTime startedAt;

	@Column(name = "completed_at")
	private OffsetDateTime completedAt;

	protected QuizAttempt() {
	}

	public QuizAttempt(AppUser user, Difficulty difficulty, OffsetDateTime startedAt) {
		this.id = UUID.randomUUID();
		this.user = user;
		this.difficulty = difficulty;
		this.status = QuizAttemptStatus.IN_PROGRESS;
		this.totalQuestions = TOTAL_QUESTIONS;
		this.startedAt = startedAt;
	}

	public UUID getId() {
		return id;
	}

	public AppUser getUser() {
		return user;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public QuizAttemptStatus getStatus() {
		return status;
	}

	public short getTotalQuestions() {
		return totalQuestions;
	}

	public Short getCorrectAnswers() {
		return correctAnswers;
	}

	public BigDecimal getScore() {
		return score;
	}

	public OffsetDateTime getStartedAt() {
		return startedAt;
	}

	public OffsetDateTime getCompletedAt() {
		return completedAt;
	}
}
