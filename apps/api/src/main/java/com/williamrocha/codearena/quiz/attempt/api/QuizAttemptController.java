package com.williamrocha.codearena.quiz.attempt.api;

import java.net.URI;
import java.util.UUID;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.williamrocha.codearena.quiz.attempt.application.CreatedQuizAttempt;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptAnswerService;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptCreationService;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptCompletionService;
import com.williamrocha.codearena.quiz.attempt.application.CompletedQuizAttempt;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptDetails;
import com.williamrocha.codearena.quiz.attempt.application.QuizAttemptQueryService;
import com.williamrocha.codearena.quiz.attempt.application.SavedQuizAnswer;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/quiz-attempts")
@ConditionalOnBean(QuizAttemptCreationService.class)
public class QuizAttemptController {

	private final QuizAttemptCreationService creationService;
	private final QuizAttemptQueryService queryService;
	private final QuizAttemptAnswerService answerService;
	private final QuizAttemptCompletionService completionService;

	public QuizAttemptController(
		QuizAttemptCreationService creationService,
		QuizAttemptQueryService queryService,
		QuizAttemptAnswerService answerService,
		QuizAttemptCompletionService completionService
	) {
		this.creationService = creationService;
		this.queryService = queryService;
		this.answerService = answerService;
		this.completionService = completionService;
	}

	@PostMapping("/{attemptId}/completion")
	CompleteQuizAttemptResponse complete(@PathVariable UUID attemptId) {
		CompletedQuizAttempt completed = completionService.complete(attemptId);

		return CompleteQuizAttemptResponse.from(completed);
	}

	@PutMapping("/{attemptId}/answers/{questionId}")
	SaveQuizAnswerResponse saveAnswer(
		@PathVariable UUID attemptId,
		@PathVariable UUID questionId,
		@Valid @RequestBody SaveQuizAnswerRequest request
	) {
		SavedQuizAnswer saved = answerService.save(
			attemptId,
			questionId,
			request.selectedAlternativeId());

		return new SaveQuizAnswerResponse(
			saved.questionId(),
			saved.selectedAlternativeId(),
			saved.answeredAt());
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
