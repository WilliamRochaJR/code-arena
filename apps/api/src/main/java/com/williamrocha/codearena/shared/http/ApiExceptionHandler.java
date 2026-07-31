package com.williamrocha.codearena.shared.http;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class ApiExceptionHandler {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(ApiExceptionHandler.class);
	private static final URI VALIDATION_ERROR_TYPE =
		URI.create("https://code-arena.dev/problems/validation-error");
	private static final URI INTERNAL_ERROR_TYPE =
		URI.create("https://code-arena.dev/problems/internal-error");

	@ExceptionHandler(ApiException.class)
	ResponseEntity<ProblemDetail> handleApiException(
		ApiException exception,
		HttpServletRequest request
	) {
		ProblemDetail problem = createProblem(
			exception.getStatus(),
			exception.getType(),
			exception.getTitle(),
			exception.getMessage(),
			request);

		return ResponseEntity
			.status(exception.getStatus())
			.body(problem);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(
		MethodArgumentNotValidException exception,
		HttpServletRequest request
	) {
		List<FieldValidationError> errors = exception
			.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(error -> new FieldValidationError(
				error.getField(),
				error.getDefaultMessage()))
			.toList();

		ProblemDetail problem = createProblem(
			HttpStatus.BAD_REQUEST,
			VALIDATION_ERROR_TYPE,
			"Dados invalidos",
			"Um ou mais campos possuem valores invalidos.",
			request);
		problem.setProperty("errors", errors);

		return ResponseEntity.badRequest().body(problem);
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ProblemDetail> handleUnexpectedException(
		Exception exception,
		HttpServletRequest request
	) {
		LOGGER.error(
			"Unexpected {} while handling {}",
			exception.getClass().getSimpleName(),
			request.getRequestURI());

		ProblemDetail problem = createProblem(
			HttpStatus.INTERNAL_SERVER_ERROR,
			INTERNAL_ERROR_TYPE,
			"Erro interno",
			"Ocorreu um erro interno inesperado.",
			request);

		return ResponseEntity.internalServerError().body(problem);
	}

	private ProblemDetail createProblem(
		HttpStatus status,
		URI type,
		String title,
		String detail,
		HttpServletRequest request
	) {
		ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
		problem.setType(type);
		problem.setTitle(title);
		problem.setInstance(URI.create(request.getRequestURI()));
		return problem;
	}
}
