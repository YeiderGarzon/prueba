package com.prueba.prueba.application.dto;

import com.prueba.prueba.domain.model.UserRole;

public record AuthResponse(
		String accessToken,
		String tokenType,
		long expiresIn,
		String username,
		UserRole role) {
}
