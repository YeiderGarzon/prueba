package com.prueba.prueba.infrastructure.web.dto;

import com.prueba.prueba.application.dto.UserProfileUpdateRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserProfilePayload(
		@NotBlank @Pattern(regexp = "^[a-zA-Z0-9._-]{3,40}$") String username,
		@NotBlank @Size(max = 120) String fullName,
		@Pattern(regexp = "^$|.{12,72}") String password) {
	public UserProfileUpdateRequest toCommand() {
		return new UserProfileUpdateRequest(username, fullName, password);
	}
}
