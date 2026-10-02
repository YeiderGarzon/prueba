package com.prueba.prueba.infrastructure.web;

import java.util.List;

import com.prueba.prueba.application.dto.UserResponse;
import com.prueba.prueba.application.port.in.UserUseCases;
import com.prueba.prueba.infrastructure.web.dto.UserProfilePayload;
import com.prueba.prueba.infrastructure.web.dto.UserPayload;
import com.prueba.prueba.infrastructure.web.dto.UserUpdatePayload;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
	private final UserUseCases service;

	public UserController(UserUseCases service) {
		this.service = service;
	}

	@GetMapping("/me")
	public UserResponse findCurrent(Authentication authentication) {
		return service.findCurrent(authentication.getName());
	}

	@PutMapping("/me")
	public UserResponse updateCurrent(
			@Valid @RequestBody UserProfilePayload request,
			Authentication authentication) {
		return service.updateCurrent(authentication.getName(), request.toCommand());
	}

	@GetMapping
	public List<UserResponse> findAll() {
		return service.findAll();
	}

	@GetMapping("/{id}")
	public UserResponse findById(@PathVariable Long id) {
		return service.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse create(@Valid @RequestBody UserPayload request) {
		return service.create(request.toCommand());
	}

	@PutMapping("/{id}")
	public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdatePayload request) {
		return service.update(id, request.toCommand());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication.getName());
	}
}
