package com.williamrocha.codearena;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.williamrocha.codearena.identity.CurrentUserService;
import com.williamrocha.codearena.quiz.persistence.entity.AppUser;
import com.williamrocha.codearena.quiz.persistence.repository.AppUserRepository;

import static org.assertj.core.api.Assertions.assertThat;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CurrentUserServiceIntegrationTests {

	@Autowired
	private CurrentUserService currentUserService;

	@Autowired
	private AppUserRepository appUserRepository;

	@Test
	void createsControlledUserOnlyOnce() {
		AppUser firstResolution = currentUserService.getOrCreateCurrentUser();
		AppUser secondResolution = currentUserService.getOrCreateCurrentUser();

		assertThat(secondResolution.getId()).isEqualTo(firstResolution.getId());
		assertThat(appUserRepository.findAll())
			.singleElement()
			.satisfies(user -> {
				assertThat(user.getIdentityProviderSubject()).isEqualTo("test-user");
				assertThat(user.getEmail()).isEqualTo("developer@test.invalid");
				assertThat(user.getDisplayName()).isEqualTo("Test Developer");
			});
	}
}
