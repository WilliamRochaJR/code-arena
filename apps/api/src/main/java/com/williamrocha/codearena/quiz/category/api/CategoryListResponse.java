package com.williamrocha.codearena.quiz.category.api;

import java.util.List;

public record CategoryListResponse(
	List<CategoryResponse> items
) {

	public CategoryListResponse {
		items = List.copyOf(items);
	}
}
