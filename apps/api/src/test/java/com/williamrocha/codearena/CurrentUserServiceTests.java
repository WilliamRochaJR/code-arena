package com.williamrocha.codearena;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.williamrocha.codearena.identity.CurrentUserIdentity;
import com.williamrocha.codearena.identity.CurrentUserProvider;
import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.AppUser;
import com.williamrocha.codearena.quiz.persistence.repository.AppUserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTests {

	private static final Instant NOW = Instant.parse("2026-07-31T12:00:00Z");
	private static final CurrentUserIdentity IDENTITY = new CurrentUserIdentity(
		"controlled-subject",
		"developer@test.invalid",
		"Test Developer");

	@Mock
	private CurrentUserProvider currentUserProvider;

	@Mock
	private AppUserRepository appUserRepository;

	private CurrentUserService currentUserService;

	@BeforeEach
	void setUp() {
		currentUserService = new CurrentUserService(
			currentUserProvider,
			appUserRepository,
			Clock.fixed(NOW, ZoneOffset.UTC));
	}

	@Test
	void returnsExistingUserWithoutCreatingAnotherOne() {
		AppUser existingUser = new AppUser(
			IDENTITY.subject(),
			IDENTITY.email(),
			IDENTITY.displayName(),
			OffsetDateTime.now(Clock.fixed(NOW, ZoneOffset.UTC)));
		when(currentUserProvider.getCurrentUser()).thenReturn(IDENTITY);
		when(appUserRepository.findByIdentityProviderSubject(IDENTITY.subject()))
			.thenReturn(Optional.of(existingUser));

		assertThat(currentUserService.getOrCreateCurrentUser())
			.isSameAs(existingUser);
		verify(appUserRepository, never()).save(any());
	}

	@Test
	void createsUserFromCurrentIdentityWithUtcTimestamp() {
		when(currentUserProvider.getCurrentUser()).thenReturn(IDENTITY);
		when(appUserRepository.findByIdentityProviderSubject(IDENTITY.subject()))
			.thenReturn(Optional.empty());
		when(appUserRepository.save(any()))
			.thenAnswer(invocation -> invocation.getArgument(0));

		AppUser createdUser = currentUserService.getOrCreateCurrentUser();

		ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
		verify(appUserRepository).save(captor.capture());
		assertThat(createdUser).isSameAs(captor.getValue());
		assertThat(createdUser.getIdentityProviderSubject())
			.isEqualTo(IDENTITY.subject());
		assertThat(createdUser.getEmail()).isEqualTo(IDENTITY.email());
		assertThat(createdUser.getDisplayName()).isEqualTo(IDENTITY.displayName());
		assertThat(createdUser.getCreatedAt())
			.isEqualTo(OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC));
		assertThat(createdUser.getLastLoginAt())
			.isEqualTo(createdUser.getCreatedAt());
	}
}
