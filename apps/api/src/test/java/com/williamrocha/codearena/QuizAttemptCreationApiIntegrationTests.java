package com.williamrocha.codearena;

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

import com.williamrocha.codearena.quiz.persistence.repository.AttemptQuestionRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class QuizAttemptCreationApiIntegrationTests {

	private static final String ATTEMPTS_PATH = "/api/v1/quiz-attempts";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private QuizAttemptRepository quizAttemptRepository;

	@Autowired
	private QuizAttemptCategoryRepository attemptCategoryRepository;

	@Autowired
	private AttemptQuestionRepository attemptQuestionRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void createsAttemptWithTenOrderedQuestions() throws Exception {
		MvcResult result = mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "difficulty": "INTERMEDIATE",
					  "categories": ["OOP", "COLLECTIONS"]
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(header().string(
				"Location",
				org.hamcrest.Matchers.matchesPattern(
					"/api/v1/quiz-attempts/[0-9a-f-]{36}")))
			.andExpect(jsonPath("$.status").value("IN_PROGRESS"))
			.andExpect(jsonPath("$.difficulty").value("INTERMEDIATE"))
			.andExpect(jsonPath("$.categories[0]").value("OOP"))
			.andExpect(jsonPath("$.categories[1]").value("COLLECTIONS"))
			.andExpect(jsonPath("$.totalQuestions").value(10))
			.andExpect(jsonPath("$.answeredQuestions").value(0))
			.andExpect(content().string(not(containsString("correct"))))
			.andExpect(content().string(not(containsString("explanation"))))
			.andReturn();

		String location = result.getResponse().getHeader("Location");
		UUID attemptId = UUID.fromString(location.substring(
			location.lastIndexOf('/') + 1));

		assertThat(quizAttemptRepository.findById(attemptId)).isPresent();
		assertThat(attemptCategoryRepository.findAllByAttemptId(attemptId))
			.hasSize(2);
		assertThat(attemptQuestionRepository
			.findAllByAttemptIdOrderByPositionAsc(attemptId))
			.hasSize(10)
			.extracting(question -> question.getPosition())
			.containsExactly(
				(short) 1, (short) 2, (short) 3, (short) 4, (short) 5,
				(short) 6, (short) 7, (short) 8, (short) 9, (short) 10);
	}

	@Test
	void rejectsInvalidRequestBody() throws Exception {
		mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"difficulty": null, "categories": []}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.type")
				.value("https://code-arena.dev/problems/validation-error"))
			.andExpect(jsonPath("$.errors.length()").value(2));

		mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"difficulty": "UNKNOWN", "categories": ["OOP"]}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.detail")
				.value("O corpo da requisicao possui JSON ou valores invalidos."));
	}

	@Test
	void rejectsUnknownOrInactiveCategory() throws Exception {
		mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"difficulty": "BEGINNER", "categories": ["UNKNOWN"]}
					"""))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.detail")
				.value("Uma ou mais categorias nao existem ou estao inativas."));
	}

	@Test
	void rejectsFiltersWithoutTenEligibleQuestions() throws Exception {
		jdbcTemplate.update("""
			UPDATE questions
			SET active = FALSE
			WHERE difficulty = 'BEGINNER'
			""");

		mockMvc.perform(post(ATTEMPTS_PATH)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"difficulty": "BEGINNER", "categories": ["OOP"]}
					"""))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.detail").value(
				"Nao existem dez questoes compativeis com os filtros informados."));
	}
}
