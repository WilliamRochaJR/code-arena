package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptDetails;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptQueryService;
import com.williamrocha.codearena.quiz.persistence.entity.Alternative;
import com.williamrocha.codearena.quiz.persistence.entity.AppUser;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.Category;
import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.Question;
import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.repository.AlternativeRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptQueryServiceTests {

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private QuizAttemptRepository quizAttemptRepository;

	@Mock
	private AttemptQuestionRepository attemptQuestionRepository;

	@Mock
	private AlternativeRepository alternativeRepository;

	@Mock
	private QuestionCategoryRepository questionCategoryRepository;

	private QuizAttemptQueryService service;

	@BeforeEach
	void setUp() {
		service = new QuizAttemptQueryService(
			currentUserService,
			quizAttemptRepository,
			attemptQuestionRepository,
			alternativeRepository,
			questionCategoryRepository);
	}

	@Test
	void returnsOrderedPublicAttemptDetails() {
		OffsetDateTime now = OffsetDateTime.parse("2026-08-01T12:00:00Z");
		AppUser user = new AppUser("subject", "user@example.com", "User", now);
		QuizAttempt attempt = new QuizAttempt(user, Difficulty.BEGINNER, now);
		Question question = new Question(
			"O que e encapsulamento?",
			Difficulty.BEGINNER,
			"Explicacao interna",
			now);
		AttemptQuestion attemptQuestion = new AttemptQuestion(
			attempt,
			question,
			(short) 1);
		Category category = new Category("OOP", "Orientacao a objetos");
		Alternative second = new Alternative(question, "Resposta B", false, (short) 2);
		Alternative first = new Alternative(question, "Resposta A", true, (short) 1);

		when(currentUserService.getOrCreateCurrentUser()).thenReturn(user);
		when(quizAttemptRepository.findByIdAndUserId(attempt.getId(), user.getId()))
			.thenReturn(Optional.of(attempt));
		when(attemptQuestionRepository
			.findAllByAttemptIdOrderByPositionAsc(attempt.getId()))
			.thenReturn(List.of(attemptQuestion));
		when(alternativeRepository.findAllByQuestionIdIn(List.of(question.getId())))
			.thenReturn(List.of(second, first));
		when(questionCategoryRepository.findAllByQuestionIdIn(List.of(question.getId())))
			.thenReturn(List.of(new QuestionCategory(question, category)));

		QuizAttemptDetails result = service.getById(attempt.getId());

		assertThat(result.id()).isEqualTo(attempt.getId());
		assertThat(result.answeredQuestions()).isZero();
		assertThat(result.questions()).singleElement().satisfies(details -> {
			assertThat(details.statement()).isEqualTo("O que e encapsulamento?");
			assertThat(details.categories()).containsExactly("OOP");
			assertThat(details.alternatives())
				.extracting(alternative -> alternative.text())
				.containsExactly("Resposta A", "Resposta B");
			assertThat(details.selectedAlternativeId()).isNull();
		});
	}

	@Test
	void rejectsAttemptOutsideCurrentUser() {
		OffsetDateTime now = OffsetDateTime.parse("2026-08-01T12:00:00Z");
		AppUser user = new AppUser("subject", "user@example.com", "User", now);
		UUID attemptId = UUID.randomUUID();

		when(currentUserService.getOrCreateCurrentUser()).thenReturn(user);
		when(quizAttemptRepository.findByIdAndUserId(attemptId, user.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.getById(attemptId))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Tentativa nao encontrada.");
		verifyNoInteractions(
			attemptQuestionRepository,
			alternativeRepository,
			questionCategoryRepository);
	}
}
