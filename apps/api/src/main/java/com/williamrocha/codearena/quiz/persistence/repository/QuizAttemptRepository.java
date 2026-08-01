package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;

import jakarta.persistence.LockModeType;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {

	Optional<QuizAttempt> findByIdAndUserId(UUID id, UUID userId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		SELECT attempt
		FROM QuizAttempt attempt
		WHERE attempt.id = :attemptId AND attempt.user.id = :userId
		""")
	Optional<QuizAttempt> findOwnedByIdForUpdate(
		@Param("attemptId") UUID attemptId,
		@Param("userId") UUID userId
	);

	Page<QuizAttempt> findAllByUserId(UUID userId, Pageable pageable);

	Page<QuizAttempt> findAllByUserIdAndStatus(
		UUID userId,
		QuizAttemptStatus status,
		Pageable pageable
	);
}
