package com.williamrocha.codearena;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.williamrocha.codearena.identity.CurrentUserProvider;
import com.williamrocha.codearena.shared.http.ConflictException;
import com.williamrocha.codearena.shared.http.ResourceNotFoundException;
import com.williamrocha.codearena.shared.http.UnprocessableEntityException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({
	TestcontainersConfiguration.class,
	ApiFoundationIntegrationTests.TestController.class
})
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiFoundationIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CurrentUserProvider currentUserProvider;

	@Test
	void providesControlledIdentityInTestProfile() {
		assertThat(currentUserProvider.getCurrentUser())
			.satisfies(identity -> {
				assertThat(identity.subject()).isEqualTo("test-user");
				assertThat(identity.email()).isEqualTo("developer@test.invalid");
				assertThat(identity.displayName()).isEqualTo("Test Developer");
			});
	}

	@Test
	void returnsProblemDetailsForBusinessErrors() throws Exception {
		mockMvc.perform(get("/api/v1/test/problems/not-found"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.type")
				.value("https://code-arena.dev/problems/resource-not-found"))
			.andExpect(jsonPath("$.title").value("Recurso nao encontrado"))
			.andExpect(jsonPath("$.status").value(404))
			.andExpect(jsonPath("$.detail").value("Tentativa nao encontrada."))
			.andExpect(jsonPath("$.instance")
				.value("/api/v1/test/problems/not-found"));

		mockMvc.perform(get("/api/v1/test/problems/conflict"))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.type")
				.value("https://code-arena.dev/problems/conflict"));

		mockMvc.perform(get("/api/v1/test/problems/unprocessable"))
			.andExpect(status().isUnprocessableContent())
			.andExpect(jsonPath("$.type")
				.value("https://code-arena.dev/problems/unprocessable-entity"));
	}

	@Test
	void returnsFieldErrorsForInvalidRequest() throws Exception {
		mockMvc.perform(post("/api/v1/test/problems/validation")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{"name": ""}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.type")
				.value("https://code-arena.dev/problems/validation-error"))
			.andExpect(jsonPath("$.errors[0].field").value("name"))
			.andExpect(jsonPath("$.errors[0].message")
				.value("must not be blank"));
	}

	@Test
	void hidesUnexpectedErrorDetails() throws Exception {
		mockMvc.perform(get("/api/v1/test/problems/unexpected"))
			.andExpect(status().isInternalServerError())
			.andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
			.andExpect(jsonPath("$.type")
				.value("https://code-arena.dev/problems/internal-error"))
			.andExpect(jsonPath("$.detail")
				.value("Ocorreu um erro interno inesperado."))
			.andExpect(content().string(
				org.hamcrest.Matchers.not(
					org.hamcrest.Matchers.containsString("database-password"))));
	}

	@RestController
	static class TestController {

		@GetMapping("/api/v1/test/problems/not-found")
		void notFound() {
			throw new ResourceNotFoundException("Tentativa nao encontrada.");
		}

		@GetMapping("/api/v1/test/problems/conflict")
		void conflict() {
			throw new ConflictException("Tentativa ja concluida.");
		}

		@GetMapping("/api/v1/test/problems/unprocessable")
		void unprocessable() {
			throw new UnprocessableEntityException(
				"Nao existem questoes suficientes.");
		}

		@PostMapping("/api/v1/test/problems/validation")
		void validation(@Valid @RequestBody TestRequest request) {
		}

		@GetMapping("/api/v1/test/problems/unexpected")
		void unexpected() {
			throw new IllegalStateException("database-password");
		}
	}

	record TestRequest(@NotBlank String name) {
	}
}
