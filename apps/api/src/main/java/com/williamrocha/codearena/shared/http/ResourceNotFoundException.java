package com.williamrocha.codearena.shared.http;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

	public ResourceNotFoundException(String detail) {
		super(
			HttpStatus.NOT_FOUND,
			"resource-not-found",
			"Recurso nao encontrado",
			detail);
	}
}
