package com.williamrocha.codearena.quiz.attempt.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttempt;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptCategory;
import com.williamrocha.codearena.quiz.persistence.entity.QuizAttemptStatus;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptCategoryRepository;
import com.williamrocha.codearena.quiz.persistence.repository.QuizAttemptRepository;

@Service
@ConditionalOnBean(CurrentUserService.class)
public class QuizAttemptHistoryService {

	private final CurrentUserService currentUserService;
	private final QuizAttemptRepository quizAttemptRepository;
	private final QuizAttemptCategoryRepository categoryRepository;

	public QuizAttemptHistoryService(
		CurrentUserService currentUserService,
		QuizAttemptRepository quizAttemptRepository,
		QuizAttemptCategoryRepository categoryRepository
	) {
		this.currentUserService = currentUserService;
		this.quizAttemptRepository = quizAttemptRepository;
		this.categoryRepository = categoryRepository;
	}

	@Transactional(readOnly = true)
	public QuizAttemptHistory list(int page, int size, QuizAttemptStatus status) {
		UUID userId = currentUserService.getOrCreateCurrentUser().getId();
		PageRequest pageable = PageRequest.of(
			page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
		Page<QuizAttempt> attempts = status == null
			? quizAttemptRepository.findAllByUserId(userId, pageable)
			: quizAttemptRepository.findAllByUserIdAndStatus(userId, status, pageable);
		List<UUID> attemptIds = attempts.stream().map(QuizAttempt::getId).toList();
		Map<UUID, List<String>> categories = categoryRepository
			.findAllByAttemptIdIn(attemptIds).stream()
			.collect(Collectors.groupingBy(
				link -> link.getAttempt().getId(),
				Collectors.mapping(
					link -> link.getCategory().getSlug(),
					Collectors.collectingAndThen(Collectors.toList(), values ->
						values.stream().sorted().toList()))));
		List<QuizAttemptHistoryItem> items = attempts.stream()
			.map(attempt -> new QuizAttemptHistoryItem(
				attempt.getId(), attempt.getStatus(), attempt.getDifficulty(),
				categories.getOrDefault(attempt.getId(), List.of()),
				attempt.getScore(), attempt.getStartedAt(), attempt.getCompletedAt()))
			.toList();

		return new QuizAttemptHistory(
			items, attempts.getNumber(), attempts.getSize(),
			attempts.getTotalElements(), attempts.getTotalPages());
	}
}
