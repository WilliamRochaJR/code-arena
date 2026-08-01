package com.williamrocha.codearena.quiz.category.application;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.quiz.category.api.CategoryResponse;
import com.williamrocha.codearena.quiz.persistence.repository.CategoryRepository;

@Service
public class CategoryQueryService {

	private final CategoryRepository categoryRepository;

	public CategoryQueryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public List<CategoryResponse> findActiveCategories() {
		return categoryRepository
			.findAllByActiveTrueOrderByNameAsc()
			.stream()
			.map(category -> new CategoryResponse(
				category.getSlug(),
				category.getName()))
			.toList();
	}
}
