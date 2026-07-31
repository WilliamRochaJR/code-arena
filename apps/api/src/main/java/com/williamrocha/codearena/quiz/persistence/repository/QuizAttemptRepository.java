package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

	Optional<QuizAttempt> findByIdAndUserId(UUID id, UUID userId);

	Page<QuizAttempt> findAllByUserId(UUID userId, Pageable pageable);

	Page<QuizAttempt> findAllByUserIdAndStatus(
		UUID userId,
		QuizAttemptStatus status,
		Pageable pageable
	);
}
