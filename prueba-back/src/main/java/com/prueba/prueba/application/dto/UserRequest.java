package com.prueba.prueba.application.dto;

import com.prueba.prueba.domain.model.UserRole;

public record UserRequest(
		String username,
		String fullName,
		UserRole role,
		String password) {
}
