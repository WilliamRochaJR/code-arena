package com.williamrocha.codearena.identity;

import java.time.Clock;
import java.time.OffsetDateTime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.quiz.persistence.entity.AppUser;
import com.williamrocha.codearena.quiz.persistence.repository.AppUserRepository;

@Service
@ConditionalOnBean(CurrentUserProvider.class)
public class CurrentUserService {

	private final CurrentUserProvider currentUserProvider;
	private final AppUserRepository appUserRepository;
	private final Clock clock;

	public CurrentUserService(
		CurrentUserProvider currentUserProvider,
		AppUserRepository appUserRepository,
		Clock clock
	) {
		this.currentUserProvider = currentUserProvider;
		this.appUserRepository = appUserRepository;
		this.clock = clock;
	}

	@Transactional
	public AppUser getOrCreateCurrentUser() {
		CurrentUserIdentity identity = currentUserProvider.getCurrentUser();

		return appUserRepository
			.findByIdentityProviderSubject(identity.subject())
			.orElseGet(() -> appUserRepository.save(new AppUser(
				identity.subject(),
				identity.email(),
				identity.displayName(),
				OffsetDateTime.now(clock))));
	}
}
