package com.williamrocha.codearena.quiz.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.williamrocha.codearena.quiz.persistence.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

	Optional<AppUser> findByIdentityProviderSubject(String identityProviderSubject);
}
