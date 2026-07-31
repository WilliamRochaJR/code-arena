package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

	List<Category> findAllByActiveTrueOrderByNameAsc();

	List<Category> findAllByActiveTrueAndSlugIn(Collection<String> slugs);
}
