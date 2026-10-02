package com.prueba.prueba.infrastructure.web.dto;

import com.prueba.prueba.application.dto.UserRequest;
import com.prueba.prueba.domain.model.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserPayload(
		@NotBlank @Pattern(regexp = "^[a-zA-Z0-9._-]{3,40}$") String username,
		@NotBlank @Size(max = 120) String fullName,
		@NotNull UserRole role,
		@NotBlank @Size(min = 12, max = 72) String password) {
	public UserRequest toCommand() {
		return new UserRequest(username, fullName, role, password);
	}
}
