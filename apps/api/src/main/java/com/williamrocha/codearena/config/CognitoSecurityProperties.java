package com.williamrocha.codearena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("code-arena.security.cognito")
public record CognitoSecurityProperties(
	@NotBlank String issuerUri,
	@NotBlank String clientId
) {
}
