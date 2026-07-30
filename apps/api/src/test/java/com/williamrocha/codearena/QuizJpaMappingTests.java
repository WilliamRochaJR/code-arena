package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptCategory;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizJpaMappingTests {

	@Autowired
	private EntityManager entityManager;

	@Test
	void mapsAllEightDomainTablesAsEntities() {
		Set<Class<?>> mappedTypes = entityManager
			.getMetamodel()
			.getEntities()
			.stream()
			.map(EntityType::getJavaType)
			.collect(Collectors.toSet());

		assertThat(mappedTypes).containsExactlyInAnyOrder(
			AppUser.class,
			Category.class,
			Question.class,
			QuestionCategory.class,
			Alternative.class,
			QuizAttempt.class,
			QuizAttemptCategory.class,
			AttemptQuestion.class);
	}

	@Test
	void persistsAndLoadsQuizRelationships() {
		OffsetDateTime now = OffsetDateTime.now();
		AppUser user = new AppUser(
			"identity-subject",
			"developer@example.com",
			"Developer",
			now);
		Category category = new Category("oop", "Orientacao a objetos");
		Question question = new Question(
			"O que e encapsulamento?",
			Difficulty.BEGINNER,
			"Encapsulamento protege o estado interno.",
			now);
		Alternative alternative = new Alternative(
			question,
			"Ocultar detalhes internos",
			true,
			(short) 1);
		QuestionCategory questionCategory = new QuestionCategory(question, category);
		QuizAttempt attempt = new QuizAttempt(user, Difficulty.BEGINNER, now);
		QuizAttemptCategory attemptCategory = new QuizAttemptCategory(attempt, category);
		AttemptQuestion attemptQuestion = new AttemptQuestion(
			attempt,
			question,
			(short) 1);

		entityManager.persist(user);
		entityManager.persist(category);
		entityManager.persist(question);
		entityManager.persist(alternative);
		entityManager.persist(questionCategory);
		entityManager.persist(attempt);
		entityManager.persist(attemptCategory);
		entityManager.persist(attemptQuestion);
		entityManager.flush();
		entityManager.clear();

		AttemptQuestion loaded = entityManager.find(
			AttemptQuestion.class,
			attemptQuestion.getId());

		assertThat(loaded.getAttempt().getUser().getId()).isEqualTo(user.getId());
		assertThat(loaded.getQuestion().getId()).isEqualTo(question.getId());
		assertThat(loaded.getPosition()).isEqualTo((short) 1);
		assertThat(entityManager.find(
			QuestionCategory.class,
			questionCategory.getId())).isNotNull();
		assertThat(entityManager.find(
			QuizAttemptCategory.class,
			attemptCategory.getId())).isNotNull();
	}
}
