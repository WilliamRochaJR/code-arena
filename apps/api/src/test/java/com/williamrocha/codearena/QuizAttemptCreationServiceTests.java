package com.williamrocha.codearena;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptCreationService;
import com.williamrocha.codearena.quiz.persistence.entity.Category;
import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.Question;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.CategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.UnprocessableEntityException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptCreationServiceTests {

	private static final Difficulty DIFFICULTY = Difficulty.BEGINNER;
	private static final List<String> CATEGORY_SLUGS = List.of("OOP");

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private CategoryRepository categoryRepository;

	@Mock
	private QuestionRepository questionRepository;

	@Mock
	private QuizAttemptRepository quizAttemptRepository;

	@Mock
	private QuizAttemptCategoryRepository attemptCategoryRepository;

	@Mock
	private AttemptQuestionRepository attemptQuestionRepository;

	private QuizAttemptCreationService creationService;

	@BeforeEach
	void setUp() {
		creationService = new QuizAttemptCreationService(
			currentUserService,
			categoryRepository,
			questionRepository,
			quizAttemptRepository,
			attemptCategoryRepository,
			attemptQuestionRepository,
			Clock.systemUTC());
	}

	@Test
	void rejectsUnknownOrInactiveCategoriesBeforeSelectingQuestions() {
		when(categoryRepository.findAllByActiveTrueAndSlugIn(CATEGORY_SLUGS))
			.thenReturn(List.of());

		assertThatThrownBy(() -> creationService.create(
			DIFFICULTY,
			CATEGORY_SLUGS))
			.isInstanceOf(UnprocessableEntityException.class)
			.hasMessage("Uma ou mais categorias nao existem ou estao inativas.");

		verifyNoInteractions(questionRepository, currentUserService);
		verify(quizAttemptRepository, never()).save(any());
	}

	@Test
	void rejectsInsufficientCatalogBeforeCreatingAttempt() {
		Category category = new Category("OOP", "Orientacao a objetos");
		when(categoryRepository.findAllByActiveTrueAndSlugIn(CATEGORY_SLUGS))
			.thenReturn(List.of(category));
		when(questionRepository.findEligibleQuestions(
			DIFFICULTY,
			CATEGORY_SLUGS,
			QuizAttempt.TOTAL_QUESTIONS))
			.thenReturn(List.of(new Question(
				"Pergunta insuficiente",
				DIFFICULTY,
				"Explicacao",
				OffsetDateTime.now(ZoneOffset.UTC))));

		assertThatThrownBy(() -> creationService.create(
			DIFFICULTY,
			CATEGORY_SLUGS))
			.isInstanceOf(UnprocessableEntityException.class)
			.hasMessage(
				"Nao existem dez questoes compativeis com os filtros informados.");

		verifyNoInteractions(currentUserService);
		verify(quizAttemptRepository, never()).save(any());
	}
}
