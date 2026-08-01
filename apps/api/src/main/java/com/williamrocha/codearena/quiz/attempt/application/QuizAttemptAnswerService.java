package com.williamrocha.codearena.quiz.attempt.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.Alternative;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;
import com.williamrocha.codearena.quiz.persistence.repository.AlternativeRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.ConflictException;
import com.williamrocha.codearena.shared.http.ResourceNotFoundException;
import com.williamrocha.codearena.shared.http.UnprocessableEntityException;

@Service
@ConditionalOnBean(CurrentUserService.class)
public class QuizAttemptAnswerService {

	private final CurrentUserService currentUserService;
	private final QuizAttemptRepository quizAttemptRepository;
	private final AttemptQuestionRepository attemptQuestionRepository;
	private final AlternativeRepository alternativeRepository;
	private final Clock clock;

	public QuizAttemptAnswerService(
		CurrentUserService currentUserService,
		QuizAttemptRepository quizAttemptRepository,
		AttemptQuestionRepository attemptQuestionRepository,
		AlternativeRepository alternativeRepository,
		Clock clock
	) {
		this.currentUserService = currentUserService;
		this.quizAttemptRepository = quizAttemptRepository;
		this.attemptQuestionRepository = attemptQuestionRepository;
		this.alternativeRepository = alternativeRepository;
		this.clock = clock;
	}

	@Transactional
	public SavedQuizAnswer save(
		UUID attemptId,
		UUID questionId,
		UUID alternativeId
	) {
		UUID currentUserId = currentUserService.getOrCreateCurrentUser().getId();
		QuizAttempt attempt = quizAttemptRepository
			.findOwnedByIdForUpdate(attemptId, currentUserId)
			.orElseThrow(() -> new ResourceNotFoundException(
				"Tentativa nao encontrada."));

		if (attempt.getStatus() == QuizAttemptStatus.COMPLETED) {
			throw new ConflictException(
				"Uma tentativa concluida nao aceita novas respostas.");
		}

		AttemptQuestion attemptQuestion = attemptQuestionRepository
			.findByAttemptIdAndQuestionId(attemptId, questionId)
			.orElseThrow(() -> new UnprocessableEntityException(
				"A questao nao pertence a tentativa."));
		Alternative alternative = alternativeRepository
			.findByIdAndQuestionId(alternativeId, questionId)
			.orElseThrow(() -> new UnprocessableEntityException(
				"A alternativa nao pertence a questao."));

		attemptQuestion.answerWith(
			alternative.getId(),
			OffsetDateTime.now(clock));

		return new SavedQuizAnswer(
			questionId,
			attemptQuestion.getSelectedAlternativeId(),
			attemptQuestion.getAnsweredAt());
	}
}
