package com.williamrocha.codearena.identity;

public record CurrentUserIdentity(
	String subject,
	String email,
	String displayName
) {
}
