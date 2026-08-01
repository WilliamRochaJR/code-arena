package com.williamrocha.codearena.quiz.attempt.application;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.Category;
import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.Question;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptCategory;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.CategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;
import com.williamrocha.codearena.shared.http.UnprocessableEntityException;

@Service
@ConditionalOnBean(CurrentUserService.class)
public class QuizAttemptCreationService {

	private final CurrentUserService currentUserService;
	private final CategoryRepository categoryRepository;
	private final QuestionRepository questionRepository;
	private final QuizAttemptRepository quizAttemptRepository;
	private final QuizAttemptCategoryRepository attemptCategoryRepository;
	private final AttemptQuestionRepository attemptQuestionRepository;
	private final Clock clock;

	public QuizAttemptCreationService(
		CurrentUserService currentUserService,
		CategoryRepository categoryRepository,
		QuestionRepository questionRepository,
		QuizAttemptRepository quizAttemptRepository,
		QuizAttemptCategoryRepository attemptCategoryRepository,
		AttemptQuestionRepository attemptQuestionRepository,
		Clock clock
	) {
		this.currentUserService = currentUserService;
		this.categoryRepository = categoryRepository;
		this.questionRepository = questionRepository;
		this.quizAttemptRepository = quizAttemptRepository;
		this.attemptCategoryRepository = attemptCategoryRepository;
		this.attemptQuestionRepository = attemptQuestionRepository;
		this.clock = clock;
	}

	@Transactional
	public CreatedQuizAttempt create(
		Difficulty difficulty,
		List<String> requestedCategorySlugs
	) {
		List<String> categorySlugs = requestedCategorySlugs
			.stream()
			.distinct()
			.toList();
		List<Category> categories = categoryRepository
			.findAllByActiveTrueAndSlugIn(categorySlugs);

		if (categories.size() != categorySlugs.size()) {
			throw new UnprocessableEntityException(
				"Uma ou mais categorias nao existem ou estao inativas.");
		}

		List<Question> questions = questionRepository.findEligibleQuestions(
			difficulty,
			categorySlugs,
			QuizAttempt.TOTAL_QUESTIONS);

		if (questions.size() != QuizAttempt.TOTAL_QUESTIONS) {
			throw new UnprocessableEntityException(
				"Nao existem dez questoes compativeis com os filtros informados.");
		}

		QuizAttempt attempt = quizAttemptRepository.save(new QuizAttempt(
			currentUserService.getOrCreateCurrentUser(),
			difficulty,
			OffsetDateTime.now(clock)));

		attemptCategoryRepository.saveAll(categories
			.stream()
			.map(category -> new QuizAttemptCategory(attempt, category))
			.toList());
		attemptQuestionRepository.saveAll(createAttemptQuestions(
			attempt,
			questions));

		return new CreatedQuizAttempt(
			attempt.getId(),
			attempt.getStatus(),
			attempt.getDifficulty(),
			categorySlugs,
			attempt.getTotalQuestions(),
			attempt.getStartedAt());
	}

	private List<AttemptQuestion> createAttemptQuestions(
		QuizAttempt attempt,
		List<Question> questions
	) {
		return IntStream
			.range(0, questions.size())
			.mapToObj(index -> new AttemptQuestion(
				attempt,
				questions.get(index),
				(short) (index + 1)))
			.toList();
	}
}
