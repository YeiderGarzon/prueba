package com.prueba.prueba.infrastructure.persistence.entity;

import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class UserEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 40)
	private String username;

	@Column(nullable = false, length = 120)
	private String fullName;

	@Column(nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	protected UserEntity() {
	}

	public UserEntity(AppUser user) {
		this.id = user.getId();
		this.username = user.getUsername();
		this.fullName = user.getFullName();
		this.passwordHash = user.getPasswordHash();
		this.role = user.getRole();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getFullName() {
		return fullName;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public AppUser toDomain() {
		return new AppUser(id, username, fullName, passwordHash, role);
	}
}
