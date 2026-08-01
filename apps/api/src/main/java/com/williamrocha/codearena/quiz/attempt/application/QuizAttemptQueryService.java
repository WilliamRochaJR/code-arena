package com.williamrocha.codearena.quiz.attempt.application;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.Alternative;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.repository.AlternativeRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.ResourceNotFoundException;

@Service
@ConditionalOnBean(CurrentUserService.class)
public class QuizAttemptQueryService {

	private final CurrentUserService currentUserService;
	private final QuizAttemptRepository quizAttemptRepository;
	private final AttemptQuestionRepository attemptQuestionRepository;
	private final AlternativeRepository alternativeRepository;
	private final QuestionCategoryRepository questionCategoryRepository;

	public QuizAttemptQueryService(
		CurrentUserService currentUserService,
		QuizAttemptRepository quizAttemptRepository,
		AttemptQuestionRepository attemptQuestionRepository,
		AlternativeRepository alternativeRepository,
		QuestionCategoryRepository questionCategoryRepository
	) {
		this.currentUserService = currentUserService;
		this.quizAttemptRepository = quizAttemptRepository;
		this.attemptQuestionRepository = attemptQuestionRepository;
		this.alternativeRepository = alternativeRepository;
		this.questionCategoryRepository = questionCategoryRepository;
	}

	@Transactional(readOnly = true)
	public QuizAttemptDetails getById(UUID attemptId) {
		UUID currentUserId = currentUserService.getOrCreateCurrentUser().getId();
		QuizAttempt attempt = quizAttemptRepository
			.findByIdAndUserId(attemptId, currentUserId)
			.orElseThrow(() -> new ResourceNotFoundException(
				"Tentativa nao encontrada."));
		List<AttemptQuestion> attemptQuestions = attemptQuestionRepository
			.findAllByAttemptIdOrderByPositionAsc(attemptId);
		List<UUID> questionIds = attemptQuestions.stream()
			.map(attemptQuestion -> attemptQuestion.getQuestion().getId())
			.toList();

		Map<UUID, List<Alternative>> alternativesByQuestion =
			alternativeRepository.findAllByQuestionIdIn(questionIds)
				.stream()
				.sorted(Comparator.comparing(Alternative::getDisplayOrder))
				.collect(Collectors.groupingBy(
					alternative -> alternative.getQuestion().getId()));
		Map<UUID, List<QuestionCategory>> categoriesByQuestion =
			questionCategoryRepository.findAllByQuestionIdIn(questionIds)
				.stream()
				.collect(Collectors.groupingBy(
					category -> category.getQuestion().getId()));

		List<AttemptQuestionDetails> questions = attemptQuestions.stream()
			.map(attemptQuestion -> toDetails(
				attemptQuestion,
				alternativesByQuestion,
				categoriesByQuestion))
			.toList();
		short answeredQuestions = (short) attemptQuestions.stream()
			.filter(question -> question.getSelectedAlternativeId() != null)
			.count();

		return new QuizAttemptDetails(
			attempt.getId(),
			attempt.getStatus(),
			attempt.getDifficulty(),
			attempt.getTotalQuestions(),
			answeredQuestions,
			attempt.getStartedAt(),
			questions);
	}

	private AttemptQuestionDetails toDetails(
		AttemptQuestion attemptQuestion,
		Map<UUID, List<Alternative>> alternativesByQuestion,
		Map<UUID, List<QuestionCategory>> categoriesByQuestion
	) {
		UUID questionId = attemptQuestion.getQuestion().getId();

		return new AttemptQuestionDetails(
			questionId,
			attemptQuestion.getPosition(),
			attemptQuestion.getQuestion().getStatement(),
			categoriesByQuestion.getOrDefault(questionId, List.of())
				.stream()
				.map(QuestionCategory::getCategory)
				.map(category -> category.getSlug())
				.sorted()
				.toList(),
			alternativesByQuestion.getOrDefault(questionId, List.of())
				.stream()
				.map(alternative -> new AlternativeDetails(
					alternative.getId(),
					alternative.getText()))
				.toList(),
			attemptQuestion.getSelectedAlternativeId());
	}
}
