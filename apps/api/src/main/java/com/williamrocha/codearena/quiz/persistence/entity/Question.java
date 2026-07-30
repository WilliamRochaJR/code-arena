package com.williamrocha.codearena.quiz.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "questions")
public class Question {

	@Id
	private UUID id;

	@Column(nullable = false, columnDefinition = "text")
	private String statement;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Difficulty difficulty;

	@Column(nullable = false, columnDefinition = "text")
	private String explanation;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected Question() {
	}

	public Question(
		String statement,
		Difficulty difficulty,
		String explanation,
		OffsetDateTime createdAt
	) {
		this.id = UUID.randomUUID();
		this.statement = statement;
		this.difficulty = difficulty;
		this.explanation = explanation;
		this.active = true;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getStatement() {
		return statement;
	}

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public String getExplanation() {
		return explanation;
	}

	public boolean isActive() {
		return active;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
