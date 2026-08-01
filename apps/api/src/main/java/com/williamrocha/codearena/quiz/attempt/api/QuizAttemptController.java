package com.williamrocha.codearena.quiz.attempt.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.williamrocha.codearena.quiz.attempt.application.CreatedQuizAttempt;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptDetails;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptCreationService;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptQueryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/quiz-attempts")
@ConditionalOnBean(QuizAttemptCreationService.class)
public class QuizAttemptController {

	private final QuizAttemptCreationService creationService;
	private final QuizAttemptQueryService queryService;

	public QuizAttemptController(
		QuizAttemptCreationService creationService,
		QuizAttemptQueryService queryService
	) {
		this.creationService = creationService;
		this.queryService = queryService;
	}

	@GetMapping("/{attemptId}")
	QuizAttemptDetailsResponse getById(@PathVariable UUID attemptId) {
		QuizAttemptDetails details = queryService.getById(attemptId);

		return QuizAttemptDetailsResponse.from(details);
	}

	@PostMapping
	ResponseEntity<CreateQuizAttemptResponse> create(
		@Valid @RequestBody CreateQuizAttemptRequest request
	) {
		CreatedQuizAttempt created = creationService.create(
			request.difficulty(),
			request.categories());
		URI location = URI.create("/api/v1/quiz-attempts/" + created.id());

		return ResponseEntity
			.created(location)
			.body(new CreateQuizAttemptResponse(
				created.id(),
				created.status(),
				created.difficulty(),
				created.categories(),
				created.totalQuestions(),
				(short) 0,
				created.startedAt()));
	}
}
