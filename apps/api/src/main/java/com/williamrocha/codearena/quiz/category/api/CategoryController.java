package com.williamrocha.codearena.quiz.category.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.williamrocha.codearena.quiz.category.application.CategoryQueryService;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

	private final CategoryQueryService categoryQueryService;

	public CategoryController(CategoryQueryService categoryQueryService) {
		this.categoryQueryService = categoryQueryService;
	}

	@GetMapping
	CategoryListResponse findActiveCategories() {
		return new CategoryListResponse(
			categoryQueryService.findActiveCategories());
	}
}
