package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizAttemptHistoryApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void listsOnlyCurrentUserAttemptsWithPaginationAndStatusFilter() throws Exception {
		UUID currentUserId = createUser("test-user", "test@example.com");
		UUID otherUserId = createUser("other-user", "other@example.com");
		UUID oldest = createAttempt(currentUserId, "IN_PROGRESS", 1, null);
		UUID completed = createAttempt(currentUserId, "COMPLETED", 2, 80);
		UUID newest = createAttempt(currentUserId, "IN_PROGRESS", 3, null);
		createAttempt(otherUserId, "COMPLETED", 4, 100);
		linkCategory(oldest, "OOP");
		linkCategory(completed, "COLLECTIONS");
		linkCategory(newest, "STREAMS");

		mockMvc.perform(get("/api/v1/quiz-attempts?page=0&size=2"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(2))
			.andExpect(jsonPath("$.items[0].id").value(newest.toString()))
			.andExpect(jsonPath("$.items[1].id").value(completed.toString()))
			.andExpect(jsonPath("$.items[0].categories[0]").value("STREAMS"))
			.andExpect(jsonPath("$.items[0].score").doesNotExist())
			.andExpect(jsonPath("$.items[1].score").value(80.0))
			.andExpect(jsonPath("$.page").value(0))
			.andExpect(jsonPath("$.size").value(2))
			.andExpect(jsonPath("$.totalItems").value(3))
			.andExpect(jsonPath("$.totalPages").value(2));

		mockMvc.perform(get("/api/v1/quiz-attempts?status=COMPLETED"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.items.length()").value(1))
			.andExpect(jsonPath("$.items[0].id").value(completed.toString()));
	}

	@Test
	void rejectsInvalidPaginationAndStatus() throws Exception {
		mockMvc.perform(get("/api/v1/quiz-attempts?page=-1"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail").value(
				"Um ou mais parametros possuem valores invalidos."));
		mockMvc.perform(get("/api/v1/quiz-attempts?size=51"))
			.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/v1/quiz-attempts?status=UNKNOWN"))
			.andExpect(status().isBadRequest());
	}

	private UUID createUser(String subject, String email) {
		UUID id = UUID.randomUUID();
		jdbcTemplate.update("""
			INSERT INTO app_users (
			  id, identity_provider_subject, email, display_name,
			  created_at, last_login_at
			) VALUES (?, ?, ?, 'User', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
			""", id, subject, email);
		return id;
	}

	private UUID createAttempt(
		UUID userId,
		String status,
		int minute,
		Integer score
	) {
		UUID id = UUID.randomUUID();
		OffsetDateTime startedAt = OffsetDateTime.parse(
			"2026-08-01T12:0" + minute + ":00Z");
		OffsetDateTime completedAt = score == null ? null : startedAt.plusMinutes(1);
		jdbcTemplate.update("""
			INSERT INTO quiz_attempts (
			  id, user_id, difficulty, status, total_questions,
			  correct_answers, score, started_at, completed_at
			) VALUES (?, ?, 'INTERMEDIATE', ?, 10, ?, ?, ?, ?)
			""", id, userId, status, score == null ? null : score / 10,
			score, startedAt, completedAt);
		return id;
	}

	private void linkCategory(UUID attemptId, String slug) {
		jdbcTemplate.update("""
			INSERT INTO quiz_attempt_categories (attempt_id, category_id)
			SELECT ?, id FROM categories WHERE slug = ?
			""", attemptId, slug);
	}
}
