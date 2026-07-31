package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.Question;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

	@Query(
		value = """
			SELECT q.*
			FROM questions q
			WHERE q.active = TRUE
			  AND q.difficulty = :#{#difficulty.name()}
			  AND EXISTS (
			      SELECT 1
			      FROM question_categories qc
			      JOIN categories c ON c.id = qc.category_id
			      WHERE qc.question_id = q.id
			        AND c.active = TRUE
			        AND c.slug IN (:categorySlugs)
			  )
			ORDER BY random()
			LIMIT :limit
			""",
		nativeQuery = true
	)
	List<Question> findEligibleQuestions(
		@Param("difficulty") Difficulty difficulty,
		@Param("categorySlugs") Collection<String> categorySlugs,
		@Param("limit") int limit
	);
}
