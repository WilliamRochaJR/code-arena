package com.williamrocha.codearena.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public final class CognitoAccessTokenValidator
	implements OAuth2TokenValidator<Jwt> {

	private static final OAuth2Error INVALID_TOKEN_USE = new OAuth2Error(
		"invalid_token",
		"The token_use claim must be access.",
		null);
	private static final OAuth2Error INVALID_CLIENT = new OAuth2Error(
		"invalid_token",
		"The client_id claim does not match the configured Cognito client.",
		null);
	private static final OAuth2Error MISSING_SUBJECT = new OAuth2Error(
		"invalid_token",
		"The sub claim is required.",
		null);

	private final String clientId;

	public CognitoAccessTokenValidator(String clientId) {
		this.clientId = clientId;
	}

	@Override
	public OAuth2TokenValidatorResult validate(Jwt token) {
		if (!"access".equals(token.getClaimAsString("token_use"))) {
			return OAuth2TokenValidatorResult.failure(INVALID_TOKEN_USE);
		}

		if (!clientId.equals(token.getClaimAsString("client_id"))) {
			return OAuth2TokenValidatorResult.failure(INVALID_CLIENT);
		}

		if (token.getSubject() == null || token.getSubject().isBlank()) {
			return OAuth2TokenValidatorResult.failure(MISSING_SUBJECT);
		}

		return OAuth2TokenValidatorResult.success();
	}
}
