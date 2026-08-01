package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptCategoryId;

public interface QuizAttemptCategoryRepository
	extends JpaRepository<QuizAttemptCategory, QuizAttemptCategoryId> {

	List<QuizAttemptCategory> findAllByAttemptId(UUID attemptId);

	@EntityGraph(attributePaths = "category")
	List<QuizAttemptCategory> findAllByAttemptIdIn(Collection<UUID> attemptIds);
}
