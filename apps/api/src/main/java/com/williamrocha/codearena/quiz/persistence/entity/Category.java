package com.williamrocha.codearena.quiz.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 50)
	private String slug;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private boolean active;

	protected Category() {
	}

	public Category(String slug, String name) {
		this.id = UUID.randomUUID();
		this.slug = slug;
		this.name = name;
		this.active = true;
	}

	public UUID getId() {
		return id;
	}

	public String getSlug() {
		return slug;
	}

	public String getName() {
		return name;
	}

	public boolean isActive() {
		return active;
	}
}
