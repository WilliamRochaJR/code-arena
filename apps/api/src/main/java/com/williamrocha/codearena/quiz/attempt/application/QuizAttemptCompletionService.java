package com.williamrocha.codearena.quiz.attempt.application;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.Alternative;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;
import com.williamrocha.codearena.quiz.persistence.repository.AlternativeRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.ConflictException;
import com.williamrocha.codearena.shared.http.ResourceNotFoundException;

@Service
@ConditionalOnBean(CurrentUserService.class)
public class QuizAttemptCompletionService {

	private final CurrentUserService currentUserService;
	private final QuizAttemptRepository quizAttemptRepository;
	private final AttemptQuestionRepository attemptQuestionRepository;
	private final AlternativeRepository alternativeRepository;
	private final QuestionCategoryRepository questionCategoryRepository;
	private final Clock clock;

	public QuizAttemptCompletionService(
		CurrentUserService currentUserService,
		QuizAttemptRepository quizAttemptRepository,
		AttemptQuestionRepository attemptQuestionRepository,
		AlternativeRepository alternativeRepository,
		QuestionCategoryRepository questionCategoryRepository,
		Clock clock
	) {
		this.currentUserService = currentUserService;
		this.quizAttemptRepository = quizAttemptRepository;
		this.attemptQuestionRepository = attemptQuestionRepository;
		this.alternativeRepository = alternativeRepository;
		this.questionCategoryRepository = questionCategoryRepository;
		this.clock = clock;
	}

	@Transactional
	public CompletedQuizAttempt complete(UUID attemptId) {
		UUID userId = currentUserService.getOrCreateCurrentUser().getId();
		QuizAttempt attempt = quizAttemptRepository
			.findOwnedByIdForUpdate(attemptId, userId)
			.orElseThrow(() -> new ResourceNotFoundException(
				"Tentativa nao encontrada."));
		List<AttemptQuestion> questions = attemptQuestionRepository
			.findAllByAttemptIdOrderByPositionAsc(attemptId);

		if (questions.size() != attempt.getTotalQuestions()
			|| questions.stream().anyMatch(question ->
				question.getSelectedAlternativeId() == null)) {
			throw new ConflictException(
				"Todas as questoes precisam ser respondidas antes da conclusao.");
		}

		List<UUID> questionIds = questions.stream()
			.map(question -> question.getQuestion().getId())
			.toList();
		Map<UUID, Alternative> correctByQuestion = alternativeRepository
			.findAllByQuestionIdIn(questionIds).stream()
			.filter(Alternative::isCorrect)
			.collect(Collectors.toMap(
				alternative -> alternative.getQuestion().getId(),
				Function.identity()));

		if (attempt.getStatus() == QuizAttemptStatus.IN_PROGRESS) {
			questions.forEach(question -> question.markCorrect(
				question.getSelectedAlternativeId().equals(
					correctByQuestion.get(question.getQuestion().getId()).getId())));
			short correctAnswers = (short) questions.stream()
				.filter(question -> Boolean.TRUE.equals(question.getCorrect()))
				.count();
			BigDecimal score = BigDecimal.valueOf(correctAnswers)
				.multiply(BigDecimal.valueOf(100))
				.divide(BigDecimal.valueOf(attempt.getTotalQuestions()), 2,
					RoundingMode.HALF_UP);
			attempt.complete(correctAnswers, score, OffsetDateTime.now(clock));
		}

		Map<UUID, List<QuestionCategory>> categories = questionCategoryRepository
			.findAllByQuestionIdIn(questionIds).stream()
			.collect(Collectors.groupingBy(
				category -> category.getQuestion().getId()));

		return toResult(attempt, questions, correctByQuestion, categories);
	}

	private CompletedQuizAttempt toResult(
		QuizAttempt attempt,
		List<AttemptQuestion> questions,
		Map<UUID, Alternative> correctByQuestion,
		Map<UUID, List<QuestionCategory>> categoriesByQuestion
	) {
		Map<String, short[]> performance = new HashMap<>();
		questions.forEach(question -> categoriesByQuestion
			.getOrDefault(question.getQuestion().getId(), List.of())
			.forEach(link -> {
				short[] values = performance.computeIfAbsent(
					link.getCategory().getSlug(), ignored -> new short[2]);
				values[1]++;
				if (Boolean.TRUE.equals(question.getCorrect())) {
					values[0]++;
				}
			}));

		List<CategoryPerformance> categoryResults = performance.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.map(entry -> new CategoryPerformance(
				entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
			.toList();
		List<CompletedQuestion> questionResults = questions.stream()
			.map(question -> {
				Alternative correct = correctByQuestion.get(question.getQuestion().getId());
				return new CompletedQuestion(
					question.getQuestion().getId(),
					question.getPosition(),
					question.getSelectedAlternativeId(),
					correct.getId(),
					Boolean.TRUE.equals(question.getCorrect()),
					question.getQuestion().getExplanation());
			})
			.toList();

		return new CompletedQuizAttempt(
			attempt.getId(), attempt.getStatus(), attempt.getTotalQuestions(),
			attempt.getCorrectAnswers(), attempt.getScore(), attempt.getStartedAt(),
			attempt.getCompletedAt(), categoryResults, questionResults);
	}
}
