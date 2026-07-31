package com.williamrocha.codearena;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.quiz.persistence.entity.Difficulty;
import com.williamrocha.codearena.quiz.persistence.repository.QuestionRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizSeedMigrationTests {

	private static final int CATEGORY_COUNT = 3;
	private static final int QUESTION_COUNT = 36;
	private static final int ALTERNATIVE_COUNT = 144;
	private static final int QUESTIONS_PER_ATTEMPT = 10;
	private static final List<String> CATEGORY_SLUGS = List.of(
		"OOP",
		"COLLECTIONS",
		"STREAMS");

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private QuestionRepository questionRepository;

	@Test
	void seedsExpectedCatalogSize() {
		assertThat(countCategories()).isEqualTo(CATEGORY_COUNT);
		assertThat(countQuestions()).isEqualTo(QUESTION_COUNT);
		assertThat(countAlternatives()).isEqualTo(ALTERNATIVE_COUNT);
	}

	@Test
	void givesEveryQuestionCategoriesAndFourAlternativesWithOneCorrect() {
		Integer invalidQuestionCount = jdbcTemplate.queryForObject("""
			SELECT count(*)
			FROM questions q
			WHERE (
			    SELECT count(*)
			    FROM question_categories qc
			    WHERE qc.question_id = q.id
			) = 0
			   OR (
			    SELECT count(*)
			    FROM alternatives a
			    WHERE a.question_id = q.id
			) <> 4
			   OR (
			    SELECT count(*)
			    FROM alternatives a
			    WHERE a.question_id = q.id
			      AND a.correct
			) <> 1
			""", Integer.class);

		assertThat(invalidQuestionCount).isZero();
	}

	@ParameterizedTest
	@EnumSource(Difficulty.class)
	void supportsTenQuestionsForEveryCategoryAndDifficulty(
		Difficulty difficulty
	) {
		for (String categorySlug : CATEGORY_SLUGS) {
			assertThat(questionRepository.findEligibleQuestions(
				difficulty,
				List.of(categorySlug),
				QUESTIONS_PER_ATTEMPT))
				.as("%s questions for %s", difficulty, categorySlug)
				.hasSize(QUESTIONS_PER_ATTEMPT);
		}
	}

	private int countCategories() {
		return jdbcTemplate.queryForObject(
			"SELECT count(*) FROM categories",
			Integer.class);
	}

	private int countQuestions() {
		return jdbcTemplate.queryForObject(
			"SELECT count(*) FROM questions",
			Integer.class);
	}

	private int countAlternatives() {
		return jdbcTemplate.queryForObject(
			"SELECT count(*) FROM alternatives",
			Integer.class);
	}
}
