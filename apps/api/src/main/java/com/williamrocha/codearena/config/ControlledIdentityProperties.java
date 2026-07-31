package com.williamrocha.codearena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Validated
@ConfigurationProperties("code-arena.identity.controlled")
public record ControlledIdentityProperties(
	@NotBlank String subject,
	@NotBlank @Email String email,
	@NotBlank String displayName
) {
}
