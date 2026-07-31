package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.quiz.persistence.entity.Alternative;
import com.williamrocha.codearena.quiz.persistence.entity.AppUser;
import com.williamrocha.codearena.quiz.persistence.entity.AttemptQuestion;
import com.williamrocha.codearena.quiz.persistence.entity.Category;
import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.entity.Question;
import com.williamrocha.codearena.quiz.persistence.entity.QuestionCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;
import com.williamrocha.codearena.quiz.persistence.repository.AlternativeRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AppUserRepository;
import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.CategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizRepositoryTests {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private AppUserRepository appUserRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private QuestionCategoryRepository questionCategoryRepository;

	@Autowired
	private AlternativeRepository alternativeRepository;

	@Autowired
	private QuizAttemptRepository quizAttemptRepository;

	@Autowired
	private AttemptQuestionRepository attemptQuestionRepository;

	@Test
	void findsUserByIdentityProviderSubject() {
		AppUser user = saveUser("subject-123", "user@example.com");

		assertThat(appUserRepository.findByIdentityProviderSubject("subject-123"))
			.contains(user);
	}

	@Test
	void listsOnlyActiveCategoriesOrderedByName() {
		categoryRepository.save(new Category("streams", "Streams"));
		categoryRepository.save(new Category("collections", "Collections"));

		List<Category> categories =
			categoryRepository.findAllByActiveTrueOrderByNameAsc();

		assertThat(categories)
			.extracting(Category::getSlug)
			.containsExactly("collections", "streams");
	}

	@Test
	void selectsOnlyEligibleQuestionsForDifficultyAndCategories() {
		Category oop = categoryRepository.save(
			new Category("oop", "Orientacao a objetos"));
		Category collections = categoryRepository.save(
			new Category("collections", "Collections"));
		Question eligible = saveQuestion(
			"Questao elegivel",
			Difficulty.INTERMEDIATE,
			oop);
		saveQuestion(
			"Dificuldade diferente",
			Difficulty.ADVANCED,
			oop);
		saveQuestion(
			"Categoria diferente",
			Difficulty.INTERMEDIATE,
			collections);
		entityManager.flush();

		List<Question> questions = questionRepository.findEligibleQuestions(
			Difficulty.INTERMEDIATE,
			List.of("oop"),
			10);

		assertThat(questions)
			.extracting(Question::getId)
			.containsExactly(eligible.getId());
	}

	@Test
	void loadsAlternativesInDisplayOrder() {
		Category category = categoryRepository.save(
			new Category("exceptions", "Excecoes"));
		Question question = saveQuestion(
			"Ordem das alternativas",
			Difficulty.BEGINNER,
			category);
		Alternative second = alternativeRepository.save(
			new Alternative(question, "Segunda", false, (short) 2));
		Alternative first = alternativeRepository.save(
			new Alternative(question, "Primeira", true, (short) 1));

		assertThat(
			alternativeRepository.findAllByQuestionIdOrderByDisplayOrderAsc(
				question.getId()))
			.extracting(Alternative::getId)
			.containsExactly(first.getId(), second.getId());
	}

	@Test
	void scopesAttemptLookupAndHistoryToUser() {
		OffsetDateTime now = OffsetDateTime.now();
		AppUser owner = saveUser("owner", "owner@example.com");
		AppUser otherUser = saveUser("other", "other@example.com");
		QuizAttempt older = quizAttemptRepository.save(
			new QuizAttempt(owner, Difficulty.BEGINNER, now.minusMinutes(2)));
		QuizAttempt newer = quizAttemptRepository.save(
			new QuizAttempt(owner, Difficulty.INTERMEDIATE, now));
		quizAttemptRepository.save(
			new QuizAttempt(otherUser, Difficulty.ADVANCED, now.plusMinutes(1)));

		Page<QuizAttempt> history = quizAttemptRepository.findAllByUserIdAndStatus(
			owner.getId(),
			QuizAttemptStatus.IN_PROGRESS,
			PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "startedAt")));

		assertThat(
			quizAttemptRepository.findByIdAndUserId(newer.getId(), owner.getId()))
			.contains(newer);
		assertThat(
			quizAttemptRepository.findByIdAndUserId(newer.getId(), otherUser.getId()))
			.isEmpty();
		assertThat(history.getContent())
			.extracting(QuizAttempt::getId)
			.containsExactly(newer.getId(), older.getId());
	}

	@Test
	void loadsAttemptQuestionsInPositionOrderAndWithinAttempt() {
		OffsetDateTime now = OffsetDateTime.now();
		AppUser user = saveUser("attempt-owner", "attempt@example.com");
		Category category = categoryRepository.save(
			new Category("concurrency", "Concorrencia"));
		Question firstQuestion = saveQuestion(
			"Primeira questao",
			Difficulty.ADVANCED,
			category);
		Question secondQuestion = saveQuestion(
			"Segunda questao",
			Difficulty.ADVANCED,
			category);
		QuizAttempt attempt = quizAttemptRepository.save(
			new QuizAttempt(user, Difficulty.ADVANCED, now));
		QuizAttempt otherAttempt = quizAttemptRepository.save(
			new QuizAttempt(user, Difficulty.ADVANCED, now.plusSeconds(1)));
		AttemptQuestion second = attemptQuestionRepository.save(
			new AttemptQuestion(attempt, secondQuestion, (short) 2));
		AttemptQuestion first = attemptQuestionRepository.save(
			new AttemptQuestion(attempt, firstQuestion, (short) 1));

		assertThat(
			attemptQuestionRepository.findAllByAttemptIdOrderByPositionAsc(
				attempt.getId()))
			.extracting(AttemptQuestion::getId)
			.containsExactly(first.getId(), second.getId());
		assertThat(
			attemptQuestionRepository.findByAttemptIdAndQuestionId(
				attempt.getId(),
				firstQuestion.getId()))
			.contains(first);
		assertThat(
			attemptQuestionRepository.findByAttemptIdAndQuestionId(
				otherAttempt.getId(),
				firstQuestion.getId()))
			.isEmpty();
	}

	private AppUser saveUser(String subject, String email) {
		return appUserRepository.save(
			new AppUser(
				subject,
				email,
				"Developer",
				OffsetDateTime.now()));
	}

	private Question saveQuestion(
		String statement,
		Difficulty difficulty,
		Category category
	) {
		Question question = questionRepository.save(
			new Question(
				statement,
				difficulty,
				"Explicacao",
				OffsetDateTime.now()));
		questionCategoryRepository.save(
			new QuestionCategory(question, category));
		return question;
	}
}
