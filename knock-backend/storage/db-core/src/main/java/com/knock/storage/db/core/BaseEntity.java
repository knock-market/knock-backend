package com.knock.storage.db.core;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@CreationTimestamp
	@Column
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column
	private LocalDateTime updatedAt;

	@Column
	private LocalDateTime deletedAt;

	public Long getId() {
		return id;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public LocalDateTime getDeletedAt() {
		return deletedAt;
	}

	public void delete() {
		deletedAt = LocalDateTime.now();
	}

	public void restore() {
		deletedAt = null;
	}

}