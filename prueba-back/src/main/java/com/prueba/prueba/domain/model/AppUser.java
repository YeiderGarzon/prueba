package com.prueba.prueba.domain.model;

public class AppUser {
	private final Long id;
	private String username;
	private String fullName;
	private String passwordHash;
	private UserRole role;

	public AppUser(Long id, String username, String fullName, String passwordHash, UserRole role) {
		this.id = id;
		this.username = username;
		this.fullName = fullName;
		this.passwordHash = passwordHash;
		this.role = role;
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

	public void update(String username, String fullName, UserRole role, String passwordHash) {
		this.username = username;
		this.fullName = fullName;
		this.role = role;
		if (passwordHash != null) {
			this.passwordHash = passwordHash;
		}
	}
}
