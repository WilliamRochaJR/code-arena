package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategoryId;

public interface QuestionCategoryRepository
	extends JpaRepository<QuestionCategory, QuestionCategoryId> {

	@EntityGraph(attributePaths = "category")
	List<QuestionCategory> findAllByQuestionIdIn(Collection<UUID> questionIds);
}
