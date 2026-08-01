package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.Alternative;

public interface AlternativeRepository extends JpaRepository<Alternative, UUID> {

	List<Alternative> findAllByQuestionIdOrderByDisplayOrderAsc(UUID questionId);

	List<Alternative> findAllByQuestionIdIn(Collection<UUID> questionIds);
}
