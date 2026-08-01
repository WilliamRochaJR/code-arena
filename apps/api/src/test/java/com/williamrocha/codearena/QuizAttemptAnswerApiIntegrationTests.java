package com.williamrocha.codearena;

import java.time.OffsetDateTime;
import java.util.List;
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

import com.jayway.jsonpath.JsonPath;

import jakarta.persistence.EntityManager;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizAttemptAnswerApiIntegrationTests {

	private static final String ATTEMPTS_PATH = "/api/v1/quiz-attempts";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	@Test
	void createsReplacesAndRepeatsAnswerIdempotently() throws Exception {
		AttemptContext context = createAttemptContext();

		MvcResult first = saveAnswer(context, context.alternativeIds().get(0))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.questionId")
				.value(context.questionId().toString()))
			.andExpect(jsonPath("$.selectedAlternativeId")
				.value(context.alternativeIds().get(0).toString()))
			.andExpect(jsonPath("$.answeredAt").isNotEmpty())
			.andExpect(content().string(not(containsString("correct"))))
			.andReturn();
		String firstAnsweredAt = JsonPath.read(
			first.getResponse().getContentAsString(),
			"$.answeredAt");

		saveAnswer(context, context.alternativeIds().get(0))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.answeredAt").value(firstAnsweredAt));

		saveAnswer(context, context.alternativeIds().get(1))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.selectedAlternativeId")
				.value(context.alternativeIds().get(1).toString()));
	}

	@Test
	void rejectsInvalidBodyQuestionAndAlternative() throws Exception {
		AttemptContext context = createAttemptContext();

		mockMvc.perform(put(answerPath(context.attemptId(), context.questionId()))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"selectedAlternativeId\": null}"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[0].field")
				.value("selectedAlternativeId"));

		mockMvc.perform(put(answerPath(context.attemptId(), UUID.randomUUID()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(answerBody(context.alternativeIds().get(0))))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.detail")
				.value("A questao nao pertence a tentativa."));

		UUID alternativeFromAnotherQuestion = jdbcTemplate.queryForObject("""
			SELECT alternative.id
			FROM attempt_questions attempt_question
			JOIN alternatives alternative
			  ON alternative.question_id = attempt_question.question_id
			WHERE attempt_question.attempt_id = ?
			  AND attempt_question.question_id <> ?
			ORDER BY attempt_question.position, alternative.display_order
			LIMIT 1
			""", UUID.class, context.attemptId(), context.questionId());

		mockMvc.perform(put(answerPath(context.attemptId(), context.questionId()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(answerBody(alternativeFromAnotherQuestion)))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.detail")
				.value("A alternativa nao pertence a questao."));
	}

	@Test
	void rejectsCompletedOrUnknownAttempt() throws Exception {
		AttemptContext context = createAttemptContext();
		jdbcTemplate.update("""
			UPDATE quiz_attempts
			SET status = 'COMPLETED', correct_answers = 0, score = 0,
			    completed_at = started_at + INTERVAL '1 second'
			WHERE id = ?
			""", context.attemptId());
		entityManager.clear();

		saveAnswer(context, context.alternativeIds().get(0))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.detail").value(
				"Uma tentativa concluida nao aceita novas respostas."));

		mockMvc.perform(put(answerPath(UUID.randomUUID(), context.questionId()))
				.contentType(MediaType.APPLICATION_JSON)
				.content(answerBody(context.alternativeIds().get(0))))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.detail")
				.value("Tentativa nao encontrada."));
	}

	private org.springframework.test.web.servlet.ResultActions saveAnswer(
		AttemptContext context,
		UUID alternativeId
	) throws Exception {
		return mockMvc.perform(put(answerPath(
				context.attemptId(),
				context.questionId()))
			.contentType(MediaType.APPLICATION_JSON)
			.content(answerBody(alternativeId)));
	}

	private AttemptContext createAttemptContext() throws Exception {
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
		UUID attemptId = UUID.fromString(location.substring(
			location.lastIndexOf('/') + 1));
		entityManager.flush();
		UUID questionId = jdbcTemplate.queryForObject("""
			SELECT question_id
			FROM attempt_questions
			WHERE attempt_id = ?
			ORDER BY position
			LIMIT 1
			""", UUID.class, attemptId);
		List<UUID> alternativeIds = jdbcTemplate.queryForList("""
			SELECT id
			FROM alternatives
			WHERE question_id = ?
			ORDER BY display_order
			LIMIT 2
			""", UUID.class, questionId);

		return new AttemptContext(attemptId, questionId, alternativeIds);
	}

	private String answerPath(UUID attemptId, UUID questionId) {
		return ATTEMPTS_PATH + "/" + attemptId + "/answers/" + questionId;
	}

	private String answerBody(UUID alternativeId) {
		return "{\"selectedAlternativeId\":\"" + alternativeId + "\"}";
	}

	private record AttemptContext(
		UUID attemptId,
		UUID questionId,
		List<UUID> alternativeIds
	) {
	}
}
