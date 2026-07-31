package com.williamrocha.codearena.quiz.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategoryId;

public interface QuestionCategoryRepository
	extends JpaRepository<QuestionCategory, QuestionCategoryId> {
}
