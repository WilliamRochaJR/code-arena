package com.williamrocha.codearena;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptAnswerService;
import com.williamrocha.codearena.quiz.attempt.application.SavedQuizAnswer;
import com.williamrocha.codearena.quiz.persistence.entity.Alternative;
import com.williamrocha.codearena.quiz.persistence.entity.AppUser;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.Question;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.repository.AlternativeRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.ResourceNotFoundException;
import com.williamrocha.codearena.shared.http.UnprocessableEntityException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptAnswerServiceTests {

	private static final OffsetDateTime NOW =
		OffsetDateTime.parse("2026-08-01T15:00:00Z");

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private QuizAttemptRepository quizAttemptRepository;

	@Mock
	private AttemptQuestionRepository attemptQuestionRepository;

	@Mock
	private AlternativeRepository alternativeRepository;

	private QuizAttemptAnswerService service;
	private AppUser user;
	private QuizAttempt attempt;
	private Question question;
	private AttemptQuestion attemptQuestion;
	private Alternative alternative;

	@BeforeEach
	void setUp() {
		service = new QuizAttemptAnswerService(
			currentUserService,
			quizAttemptRepository,
			attemptQuestionRepository,
			alternativeRepository,
			Clock.fixed(NOW.toInstant(), ZoneOffset.UTC));
		user = new AppUser("subject", "user@example.com", "User", NOW);
		attempt = new QuizAttempt(user, Difficulty.BEGINNER, NOW.minusMinutes(1));
		question = new Question(
			"O que e encapsulamento?",
			Difficulty.BEGINNER,
			"Explicacao",
			NOW);
		attemptQuestion = new AttemptQuestion(attempt, question, (short) 1);
		alternative = new Alternative(question, "Resposta", true, (short) 1);
	}

	@Test
	void savesAnswerAndKeepsTimestampWhenRepeated() {
		stubOwnedAttempt();
		when(attemptQuestionRepository.findByAttemptIdAndQuestionId(
			attempt.getId(), question.getId()))
			.thenReturn(Optional.of(attemptQuestion));
		when(alternativeRepository.findByIdAndQuestionId(
			alternative.getId(), question.getId()))
			.thenReturn(Optional.of(alternative));

		SavedQuizAnswer first = service.save(
			attempt.getId(), question.getId(), alternative.getId());
		SavedQuizAnswer repeated = service.save(
			attempt.getId(), question.getId(), alternative.getId());

		assertThat(first.selectedAlternativeId()).isEqualTo(alternative.getId());
		assertThat(first.answeredAt()).isEqualTo(NOW);
		assertThat(repeated).isEqualTo(first);
	}

	@Test
	void rejectsQuestionOutsideAttemptBeforeLoadingAlternative() {
		stubOwnedAttempt();
		UUID unknownQuestionId = UUID.randomUUID();
		when(attemptQuestionRepository.findByAttemptIdAndQuestionId(
			attempt.getId(), unknownQuestionId))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.save(
			attempt.getId(), unknownQuestionId, alternative.getId()))
			.isInstanceOf(UnprocessableEntityException.class)
			.hasMessage("A questao nao pertence a tentativa.");
		verifyNoInteractions(alternativeRepository);
	}

	@Test
	void rejectsAlternativeOutsideQuestion() {
		stubOwnedAttempt();
		UUID unknownAlternativeId = UUID.randomUUID();
		when(attemptQuestionRepository.findByAttemptIdAndQuestionId(
			attempt.getId(), question.getId()))
			.thenReturn(Optional.of(attemptQuestion));
		when(alternativeRepository.findByIdAndQuestionId(
			unknownAlternativeId, question.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.save(
			attempt.getId(), question.getId(), unknownAlternativeId))
			.isInstanceOf(UnprocessableEntityException.class)
			.hasMessage("A alternativa nao pertence a questao.");
	}

	@Test
	void hidesAttemptOutsideCurrentUser() {
		when(currentUserService.getOrCreateCurrentUser()).thenReturn(user);
		when(quizAttemptRepository.findOwnedByIdForUpdate(
			attempt.getId(), user.getId()))
			.thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.save(
			attempt.getId(), question.getId(), alternative.getId()))
			.isInstanceOf(ResourceNotFoundException.class)
			.hasMessage("Tentativa nao encontrada.");
		verifyNoInteractions(
			attemptQuestionRepository,
			alternativeRepository);
	}

	private void stubOwnedAttempt() {
		when(currentUserService.getOrCreateCurrentUser()).thenReturn(user);
		when(quizAttemptRepository.findOwnedByIdForUpdate(
			attempt.getId(), user.getId()))
			.thenReturn(Optional.of(attempt));
	}
}
