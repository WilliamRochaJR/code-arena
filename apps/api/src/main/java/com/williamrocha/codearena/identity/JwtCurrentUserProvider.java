package com.williamrocha.codearena.identity;

import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@Profile("!local & !test")
public class JwtCurrentUserProvider implements CurrentUserProvider {

	@Override
	public CurrentUserIdentity getCurrentUser() {
		Authentication authentication = SecurityContextHolder
			.getContext()
			.getAuthentication();

		if (authentication == null
			|| !authentication.isAuthenticated()
			|| !(authentication.getPrincipal() instanceof Jwt jwt)
			|| jwt.getSubject() == null
			|| jwt.getSubject().isBlank()) {
			throw new AuthenticationCredentialsNotFoundException(
				"An authenticated access token is required.");
		}

		return new CurrentUserIdentity(
			jwt.getSubject(),
			jwt.getClaimAsString("email"),
			jwt.getClaimAsString("name"));
	}
}
