package com.prueba.prueba.application.dto;

import com.prueba.prueba.domain.model.UserRole;

public record UserUpdateRequest(
		String username,
		String fullName,
		UserRole role,
		String password) {
}
