package com.williamrocha.codearena;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import com.williamrocha.codearena.identity.JwtCurrentUserProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtCurrentUserProviderTests {

	private final JwtCurrentUserProvider provider = new JwtCurrentUserProvider();

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	void readsIdentityFromAuthenticatedJwt() {
		Jwt jwt = Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.subject("cognito-subject")
			.claim("email", "developer@example.com")
			.claim("name", "Code Arena Developer")
			.build();
		SecurityContextHolder.getContext().setAuthentication(
			new TestingAuthenticationToken(jwt, null, "ROLE_USER"));

		assertThat(provider.getCurrentUser())
			.satisfies(identity -> {
				assertThat(identity.subject()).isEqualTo("cognito-subject");
				assertThat(identity.email()).isEqualTo("developer@example.com");
				assertThat(identity.displayName()).isEqualTo("Code Arena Developer");
			});
	}

	@Test
	void acceptsJwtWithoutOptionalProfileClaims() {
		Jwt jwt = Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.subject("cognito-subject")
			.build();
		SecurityContextHolder.getContext().setAuthentication(
			new TestingAuthenticationToken(jwt, null, "ROLE_USER"));

		assertThat(provider.getCurrentUser())
			.satisfies(identity -> {
				assertThat(identity.subject()).isEqualTo("cognito-subject");
				assertThat(identity.email()).isNull();
				assertThat(identity.displayName()).isNull();
			});
	}

	@Test
	void rejectsMissingAuthentication() {
		assertThatThrownBy(provider::getCurrentUser)
			.isInstanceOf(
				org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class);
	}

	@Test
	void rejectsUnauthenticatedSecurityContext() {
		Jwt jwt = Jwt.withTokenValue("token")
			.header("alg", "RS256")
			.subject("cognito-subject")
			.build();
		SecurityContextHolder.getContext().setAuthentication(
			new TestingAuthenticationToken(jwt, null));

		assertThatThrownBy(provider::getCurrentUser)
			.isInstanceOf(
				org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class);
	}
}
