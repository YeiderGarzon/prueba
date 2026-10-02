package com.prueba.prueba.infrastructure.web.dto;

import com.prueba.prueba.application.dto.LoginRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginPayload(
		@NotBlank @Size(max = 40) String username,
		@NotBlank @Size(max = 72) String password) {
	public LoginRequest toCommand() {
		return new LoginRequest(username, password);
	}
}
