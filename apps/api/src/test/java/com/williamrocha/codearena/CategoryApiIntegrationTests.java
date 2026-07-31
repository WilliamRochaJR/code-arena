package com.williamrocha.codearena;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.quiz.persistence.entity.Category;
import com.williamrocha.codearena.quiz.persistence.repository.CategoryRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryApiIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void listsOnlyActiveCategoriesOrderedByName() throws Exception {
		Category inactive = categoryRepository.saveAndFlush(
			new Category("INACTIVE", "AAA Inactive"));
		jdbcTemplate.update(
			"UPDATE categories SET active = FALSE WHERE id = ?",
			inactive.getId());

		mockMvc.perform(get("/api/v1/categories"))
			.andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andExpect(jsonPath("$.items.length()").value(3))
			.andExpect(jsonPath("$.items[0].slug").value("COLLECTIONS"))
			.andExpect(jsonPath("$.items[0].name").value("Collections"))
			.andExpect(jsonPath("$.items[1].slug").value("OOP"))
			.andExpect(jsonPath("$.items[2].slug").value("STREAMS"))
			.andExpect(jsonPath("$.items[*].slug")
				.value(org.hamcrest.Matchers.not(
					org.hamcrest.Matchers.hasItem("INACTIVE"))));
	}
}
