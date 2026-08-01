package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizAttemptQueryApiIntegrationTests {

	private static final String ATTEMPTS_PATH = "/api/v1/quiz-attempts";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	@Test
	void returnsAttemptProgressWithoutCorrectionData() throws Exception {
		UUID attemptId = createAttempt();
		entityManager.flush();
		UUID questionId = jdbcTemplate.queryForObject("""
			SELECT question_id
			FROM attempt_questions
			WHERE attempt_id = ?
			ORDER BY position
			LIMIT 1
			""", UUID.class, attemptId);
		UUID alternativeId = jdbcTemplate.queryForObject("""
			SELECT id
			FROM alternatives
			WHERE question_id = ?
			ORDER BY display_order
			LIMIT 1
			""", UUID.class, questionId);
		jdbcTemplate.update("""
			UPDATE attempt_questions
			SET selected_alternative_id = ?, answered_at = CURRENT_TIMESTAMP
			WHERE attempt_id = ? AND question_id = ?
			""", alternativeId, attemptId, questionId);
		entityManager.clear();

		mockMvc.perform(get(ATTEMPTS_PATH + "/{attemptId}", attemptId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(attemptId.toString()))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.difficulty").value("INTERMEDIATE"))
			.andExpect(jsonPath("$.totalQuestions").value(10))
			.andExpect(jsonPath("$.answeredQuestions").value(1))
			.andExpect(jsonPath("$.questions.length()").value(10))
			.andExpect(jsonPath("$.questions[0].position").value(1))
			.andExpect(jsonPath("$.questions[0].categories.length()").isNumber())
			.andExpect(jsonPath("$.questions[0].alternatives.length()").value(4))
			.andExpect(jsonPath("$.questions[0].selectedAlternativeId")
				.value(alternativeId.toString()))
			.andExpect(jsonPath("$.questions[9].position").value(10))
			.andExpect(content().string(not(containsString("correct"))))
			.andExpect(content().string(not(containsString("explanation"))));
	}

	@Test
	void returnsNotFoundForUnknownAttempt() throws Exception {
		mockMvc.perform(get(ATTEMPTS_PATH + "/{attemptId}", UUID.randomUUID()))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.detail").value("Tentativa nao encontrada."));
	}

	@Test
	void hidesAttemptOwnedByAnotherUser() throws Exception {
		UUID userId = UUID.randomUUID();
		UUID attemptId = UUID.randomUUID();
		OffsetDateTime now = OffsetDateTime.parse("2026-08-01T12:00:00Z");
		jdbcTemplate.update("""
			INSERT INTO app_users (
			  id, identity_provider_subject, email, display_name,
			  created_at, last_login_at
			) VALUES (?, ?, ?, ?, ?, ?)
			""", userId, "other-subject", "other@example.com", "Other", now, now);
		jdbcTemplate.update("""
			INSERT INTO quiz_attempts (
			  id, user_id, difficulty, status, total_questions, started_at
			) VALUES (?, ?, 'BEGINNER', 'IN_PROGRESS', 10, ?)
			""", attemptId, userId, now);

		mockMvc.perform(get(ATTEMPTS_PATH + "/{attemptId}", attemptId))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail").value("Tentativa nao encontrada."));
	}

	private UUID createAttempt() throws Exception {
		MvcResult result = mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "difficulty": "INTERMEDIATE",
					  "categories": ["OOP", "COLLECTIONS"]
					}
					"""))
			.andExpect(status().isCreated())
			.andReturn();
		String location = result.getResponse().getHeader("Location");

		return UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
	}
}
