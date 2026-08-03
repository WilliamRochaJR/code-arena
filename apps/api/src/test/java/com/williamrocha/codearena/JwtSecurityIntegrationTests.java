package com.williamrocha.codearena;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.williamrocha.codearena.config.SecurityConfiguration;
import com.williamrocha.codearena.identity.CurrentUserProvider;
import com.williamrocha.codearena.identity.JwtCurrentUserProvider;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JwtSecurityIntegrationTests.TestController.class)
@Import({
	SecurityConfiguration.class,
	JwtCurrentUserProvider.class,
	JwtSecurityIntegrationTests.TestController.class
})
@ActiveProfiles("authenticated-test")
class JwtSecurityIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtDecoder jwtDecoder;

	@Test
	void rejectsApiRequestWithoutAccessToken() throws Exception {
		mockMvc.perform(get("/api/v1/security/current-user"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsInvalidAccessToken() throws Exception {
		when(jwtDecoder.decode("invalid-token"))
			.thenThrow(new BadJwtException("Invalid token"));

		mockMvc.perform(get("/api/v1/security/current-user")
				.header("Authorization", "Bearer invalid-token"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsExpiredAccessToken() throws Exception {
		when(jwtDecoder.decode("expired-token"))
			.thenThrow(new BadJwtException("Token expired"));

		mockMvc.perform(get("/api/v1/security/current-user")
				.header("Authorization", "Bearer expired-token"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void exposesIdentityFromAuthenticatedJwt() throws Exception {
		mockMvc.perform(get("/api/v1/security/current-user")
				.with(jwt().jwt(token -> token
					.subject("cognito-subject")
					.claim("email", "developer@example.com")
					.claim("name", "Code Arena Developer"))))
			.andExpect(status().isOk())
			.andExpect(content().string("cognito-subject"));
	}

	@RestController
	static class TestController {

		private final CurrentUserProvider currentUserProvider;

		TestController(CurrentUserProvider currentUserProvider) {
			this.currentUserProvider = currentUserProvider;
		}

		@GetMapping("/api/v1/security/current-user")
		String currentUser() {
			return currentUserProvider.getCurrentUser().subject();
		}
	}
}
