package com.williamrocha.codearena.shared.http;

public record FieldValidationError(
	String field,
	String message
) {
}
