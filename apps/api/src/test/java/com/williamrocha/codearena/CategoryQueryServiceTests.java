package com.williamrocha.codearena;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.williamrocha.codearena.quiz.category.api.CategoryResponse;
import com.williamrocha.codearena.quiz.category.application.CategoryQueryService;
import com.williamrocha.codearena.quiz.persistence.entity.Category;
import com.williamrocha.codearena.quiz.persistence.repository.CategoryRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryQueryServiceTests {

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private CategoryQueryService categoryQueryService;

	@Test
	void mapsActiveCategoriesToRestResponses() {
		when(categoryRepository.findAllByActiveTrueOrderByNameAsc())
			.thenReturn(List.of(
				new Category("COLLECTIONS", "Collections"),
				new Category("OOP", "Orientacao a objetos")));

		assertThat(categoryQueryService.findActiveCategories())
			.extracting(CategoryResponse::slug, CategoryResponse::name)
			.containsExactly(
				tuple("COLLECTIONS", "Collections"),
				tuple("OOP", "Orientacao a objetos"));

		verify(categoryRepository).findAllByActiveTrueOrderByNameAsc();
	}
}
