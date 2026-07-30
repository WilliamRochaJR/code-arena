package com.williamrocha.codearena.quiz.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class AppUser {

	@Id
	private UUID id;

	@Column(name = "identity_provider_subject", nullable = false, unique = true, length = 255)
	private String identityProviderSubject;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "display_name", length = 120)
	private String displayName;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "last_login_at", nullable = false)
	private OffsetDateTime lastLoginAt;

	protected AppUser() {
	}

	public AppUser(
		String identityProviderSubject,
		String email,
		String displayName,
		OffsetDateTime createdAt
	) {
		this.id = UUID.randomUUID();
		this.identityProviderSubject = identityProviderSubject;
		this.email = email;
		this.displayName = displayName;
		this.createdAt = createdAt;
		this.lastLoginAt = createdAt;
	}

	public UUID getId() {
		return id;
	}

	public String getIdentityProviderSubject() {
		return identityProviderSubject;
	}

	public String getEmail() {
		return email;
	}

	public String getDisplayName() {
		return displayName;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getLastLoginAt() {
		return lastLoginAt;
	}
}
