package com.williamrocha.codearena;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import com.williamrocha.codearena.config.CognitoAccessTokenValidator;

import static org.assertj.core.api.Assertions.assertThat;

class CognitoAccessTokenValidatorTests {

	private static final String CLIENT_ID = "code-arena-web";
	private final CognitoAccessTokenValidator validator =
		new CognitoAccessTokenValidator(CLIENT_ID);

	@Test
	void acceptsAccessTokenForConfiguredClient() {
		assertThat(validator.validate(jwt("access", CLIENT_ID)).hasErrors())
			.isFalse();
	}

	@Test
	void rejectsIdToken() {
		assertThat(validator.validate(jwt("id", CLIENT_ID)).hasErrors())
			.isTrue();
	}

	@Test
	void rejectsAccessTokenForAnotherClient() {
		assertThat(validator.validate(jwt("access", "another-client")).hasErrors())
			.isTrue();
	}

	@Test
	void rejectsAccessTokenWithoutSubject() {
		Instant now = Instant.now();
		Jwt jwt = Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.claim("token_use", "access")
			.claim("client_id", CLIENT_ID)
			.issuedAt(now)
			.expiresAt(now.plusSeconds(300))
			.build();

		assertThat(validator.validate(jwt).hasErrors()).isTrue();
	}

	private Jwt jwt(String tokenUse, String clientId) {
		Instant now = Instant.now();
		return Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.subject("user-subject")
			.claim("token_use", tokenUse)
			.claim("client_id", clientId)
			.issuedAt(now)
			.expiresAt(now.plusSeconds(300))
			.build();
	}
}
