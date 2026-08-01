package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;

public interface AttemptQuestionRepository
	extends JpaRepository<AttemptQuestion, UUID> {

	@EntityGraph(attributePaths = "question")
	List<AttemptQuestion> findAllByAttemptIdOrderByPositionAsc(UUID attemptId);

	Optional<AttemptQuestion> findByAttemptIdAndQuestionId(
		UUID attemptId,
		UUID questionId
	);
}
