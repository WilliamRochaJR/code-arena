package com.williamrocha.codearena.shared.http;

import java.net.URI;

import org.springframework.http.HttpStatus;

public abstract class ApiException extends RuntimeException {

	private final HttpStatus status;
	private final URI type;
	private final String title;

	protected ApiException(
		HttpStatus status,
		String problemType,
		String title,
		String detail
	) {
		super(detail);
		this.status = status;
		this.type = URI.create("https://code-arena.dev/problems/" + problemType);
		this.title = title;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public URI getType() {
		return type;
	}

	public String getTitle() {
		return title;
	}
}
