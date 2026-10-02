package com.prueba.prueba.infrastructure.web;

import com.prueba.prueba.application.dto.AuthResponse;
import com.prueba.prueba.application.dto.UserProfile;
import com.prueba.prueba.application.port.in.AuthUseCases;
import com.prueba.prueba.application.port.in.UserUseCases;
import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.infrastructure.web.dto.LoginPayload;
import com.prueba.prueba.infrastructure.web.dto.RegisterPayload;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	private final AuthUseCases service;
	private final UserUseCases users;

	public AuthController(AuthUseCases service, UserUseCases users) {
		this.service = service;
		this.users = users;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public void register(@Valid @RequestBody RegisterPayload request) {
		service.register(request.toCommand());
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginPayload request) {
		return service.login(request.toCommand());
	}

	@GetMapping("/me")
	public UserProfile currentUser(Authentication authentication) {
		UserRole role = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
						? UserRole.ADMIN
						: UserRole.CUSTOMER;
		var user = users.findCurrent(authentication.getName());
		return new UserProfile(user.username(), user.fullName(), role);
	}
}
