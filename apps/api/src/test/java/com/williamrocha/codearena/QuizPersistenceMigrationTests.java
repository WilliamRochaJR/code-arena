package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class QuizPersistenceMigrationTests {

	private static final int DOMAIN_TABLE_COUNT = 8;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void createsAllDomainTables() {
		Integer tableCount = jdbcTemplate.queryForObject("""
			SELECT count(*)
			FROM information_schema.tables
			WHERE table_schema = 'public'
			  AND table_name IN (
			      'app_users',
			      'categories',
			      'questions',
			      'question_categories',
			      'alternatives',
			      'quiz_attempts',
			      'quiz_attempt_categories',
			      'attempt_questions'
			  )
			""", Integer.class);

		assertThat(tableCount).isEqualTo(DOMAIN_TABLE_COUNT);
	}

	@Test
	void rejectsCompletedAttemptWithoutPersistedResult() {
		UUID userId = insertUser();

		assertThatThrownBy(() -> jdbcTemplate.update("""
			INSERT INTO quiz_attempts (
			    id,
			    user_id,
			    difficulty,
			    status,
			    total_questions,
			    started_at,
			    completed_at
			)
			VALUES (?, ?, 'INTERMEDIATE', 'COMPLETED', 10, ?, ?)
			""",
			UUID.randomUUID(),
			userId,
			OffsetDateTime.now().minusMinutes(10),
			OffsetDateTime.now()))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsDuplicatePositionWithinAttempt() {
		UUID attemptId = insertAttempt(insertUser());
		UUID firstQuestionId = insertQuestion();
		UUID secondQuestionId = insertQuestion();

		insertAttemptQuestion(attemptId, firstQuestionId, 1, null);

		assertThatThrownBy(
			() -> insertAttemptQuestion(attemptId, secondQuestionId, 1, null))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsAlternativeFromAnotherQuestion() {
		UUID attemptId = insertAttempt(insertUser());
		UUID attemptedQuestionId = insertQuestion();
		UUID otherQuestionId = insertQuestion();
		UUID otherQuestionAlternativeId = insertAlternative(
			otherQuestionId,
			1,
			true);

		assertThatThrownBy(() -> insertAttemptQuestion(
			attemptId,
			attemptedQuestionId,
			1,
			otherQuestionAlternativeId))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	void rejectsMoreThanOneCorrectAlternativeForQuestion() {
		UUID questionId = insertQuestion();
		insertAlternative(questionId, 1, true);

		assertThatThrownBy(() -> insertAlternative(questionId, 2, true))
			.isInstanceOf(DataIntegrityViolationException.class);
	}

	private UUID insertUser() {
		UUID userId = UUID.randomUUID();
		OffsetDateTime now = OffsetDateTime.now();

		jdbcTemplate.update("""
			INSERT INTO app_users (
			    id,
			    identity_provider_subject,
			    email,
			    created_at,
			    last_login_at
			)
			VALUES (?, ?, ?, ?, ?)
			""",
			userId,
			"subject-" + userId,
			userId + "@example.com",
			now,
			now);

		return userId;
	}

	private UUID insertQuestion() {
		UUID questionId = UUID.randomUUID();
		OffsetDateTime now = OffsetDateTime.now();

		jdbcTemplate.update("""
			INSERT INTO questions (
			    id,
			    statement,
			    difficulty,
			    explanation,
			    created_at,
			    updated_at
			)
			VALUES (?, ?, 'INTERMEDIATE', ?, ?, ?)
			""",
			questionId,
			"Question " + questionId,
			"Explanation " + questionId,
			now,
			now);

		return questionId;
	}

	private UUID insertAlternative(
		UUID questionId,
		int displayOrder,
		boolean correct
	) {
		UUID alternativeId = UUID.randomUUID();

		jdbcTemplate.update("""
			INSERT INTO alternatives (
			    id,
			    question_id,
			    text,
			    correct,
			    display_order
			)
			VALUES (?, ?, ?, ?, ?)
			""",
			alternativeId,
			questionId,
			"Alternative " + alternativeId,
			correct,
			displayOrder);

		return alternativeId;
	}

	private UUID insertAttempt(UUID userId) {
		UUID attemptId = UUID.randomUUID();

		jdbcTemplate.update("""
			INSERT INTO quiz_attempts (
			    id,
			    user_id,
			    difficulty,
			    status,
			    total_questions,
			    started_at
			)
			VALUES (?, ?, 'INTERMEDIATE', 'IN_PROGRESS', 10, ?)
			""",
			attemptId,
			userId,
			OffsetDateTime.now());

		return attemptId;
	}

	private void insertAttemptQuestion(
		UUID attemptId,
		UUID questionId,
		int position,
		UUID selectedAlternativeId
	) {
		OffsetDateTime answeredAt = selectedAlternativeId == null
			? null
			: OffsetDateTime.now();

		jdbcTemplate.update("""
			INSERT INTO attempt_questions (
			    id,
			    attempt_id,
			    question_id,
			    position,
			    selected_alternative_id,
			    answered_at
			)
			VALUES (?, ?, ?, ?, ?, ?)
			""",
			UUID.randomUUID(),
			attemptId,
			questionId,
			position,
			selectedAlternativeId,
			answeredAt);
	}

}
