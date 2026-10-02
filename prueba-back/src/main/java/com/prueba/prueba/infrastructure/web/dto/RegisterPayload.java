package com.prueba.prueba.infrastructure.web.dto;

import com.prueba.prueba.application.dto.RegisterRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPayload(
		@NotBlank @Pattern(regexp = "^[a-zA-Z0-9._-]{3,40}$") String username,
		@NotBlank @Size(max = 120) String fullName,
		@NotBlank @Size(min = 12, max = 72) String password) {
	public RegisterRequest toCommand() {
		return new RegisterRequest(username, fullName, password);
	}
}
