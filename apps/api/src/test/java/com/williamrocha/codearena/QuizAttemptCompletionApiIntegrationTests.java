package com.williamrocha.codearena;

import java.util.List;
import java.util.Map;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizAttemptCompletionApiIntegrationTests {

	private static final String ATTEMPTS_PATH = "/api/v1/quiz-attempts";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	@Test
	void completesAndReturnsPersistedResultIdempotently() throws Exception {
		UUID attemptId = createAttempt();
		List<Map<String, Object>> questions = jdbcTemplate.queryForList("""
			SELECT aq.question_id, correct.id AS correct_id, incorrect.id AS incorrect_id
			FROM attempt_questions aq
			JOIN alternatives correct
			  ON correct.question_id = aq.question_id AND correct.correct = TRUE
			JOIN LATERAL (
			  SELECT id FROM alternatives
			  WHERE question_id = aq.question_id AND correct = FALSE
			  ORDER BY display_order LIMIT 1
			) incorrect ON TRUE
			WHERE aq.attempt_id = ? ORDER BY aq.position
			""", attemptId);

		for (int index = 0; index < questions.size(); index++) {
			Map<String, Object> question = questions.get(index);
			UUID questionId = (UUID) question.get("question_id");
			UUID alternativeId = (UUID) question.get(
				index < 8 ? "correct_id" : "incorrect_id");
			mockMvc.perform(put(ATTEMPTS_PATH +
					"/{attemptId}/answers/{questionId}", attemptId, questionId)
					.contentType(MediaType.APPLICATION_JSON)
					.content("{\"selectedAlternativeId\":\"" + alternativeId + "\"}"))
				.andExpect(status().isOk());
		}

		MvcResult first = mockMvc.perform(post(
				ATTEMPTS_PATH + "/{attemptId}/completion", attemptId))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status").value("COMPLETED"))
			.andExpect(jsonPath("$.correctAnswers").value(8))
			.andExpect(jsonPath("$.score").value(80.0))
			.andExpect(jsonPath("$.completedAt").isNotEmpty())
			.andExpect(jsonPath("$.performanceByCategory").isNotEmpty())
			.andExpect(jsonPath("$.questions.length()").value(10))
			.andExpect(jsonPath("$.questions[0].correctAlternativeId").isNotEmpty())
			.andExpect(jsonPath("$.questions[0].correct").value(true))
			.andExpect(jsonPath("$.questions[0].explanation").isNotEmpty())
			.andExpect(jsonPath("$.questions[9].correct").value(false))
			.andReturn();

		MvcResult repeated = mockMvc.perform(post(
				ATTEMPTS_PATH + "/{attemptId}/completion", attemptId))
			.andExpect(status().isOk())
			.andReturn();
		assertThat(repeated.getResponse().getContentAsString())
			.isEqualTo(first.getResponse().getContentAsString());
	}

	@Test
	void rejectsIncompleteOrUnknownAttempt() throws Exception {
		UUID attemptId = createAttempt();
		mockMvc.perform(post(ATTEMPTS_PATH + "/{attemptId}/completion", attemptId))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value(
				"Todas as questoes precisam ser respondidas antes da conclusao."));
		mockMvc.perform(post(ATTEMPTS_PATH + "/{attemptId}/completion",
				UUID.randomUUID()))
			.andExpect(status().isNotFound());
	}

	private UUID createAttempt() throws Exception {
		MvcResult result = mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"difficulty":"INTERMEDIATE","categories":["OOP","COLLECTIONS"]}
					"""))
			.andExpect(status().isCreated()).andReturn();
		String location = result.getResponse().getHeader("Location");
		UUID attemptId = UUID.fromString(location.substring(location.lastIndexOf('/') + 1));
		entityManager.flush();
		entityManager.clear();
		return attemptId;
	}
}
