package com.williamrocha.codearena.shared.http;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException {

	public UnprocessableEntityException(String detail) {
		super(
			HttpStatus.UNPROCESSABLE_CONTENT,
			"unprocessable-entity",
			"Regra de negocio nao atendida",
			detail);
	}
}
