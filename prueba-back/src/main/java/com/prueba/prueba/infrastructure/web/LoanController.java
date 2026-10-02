package com.prueba.prueba.infrastructure.web;

import java.util.List;

import com.prueba.prueba.application.dto.LoanResponse;
import com.prueba.prueba.application.port.in.LoanUseCases;
import com.prueba.prueba.infrastructure.web.dto.LoanDecisionPayload;
import com.prueba.prueba.infrastructure.web.dto.LoanPayload;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
	private final LoanUseCases service;

	public LoanController(LoanUseCases service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<LoanResponse> create(@Valid @RequestBody LoanPayload request, Authentication authentication) {
		String applicantName = authentication.getName();
		return Mono.fromCallable(() -> service.create(applicantName, request.toCommand()))
				.subscribeOn(Schedulers.boundedElastic());
	}

	@GetMapping
	public List<LoanResponse> findAll(Authentication authentication) {
		boolean administrator = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
		return service.findAll(authentication.getName(), administrator);
	}

	@GetMapping("/{id}")
	public LoanResponse findById(@PathVariable Long id, Authentication authentication) {
		boolean administrator = authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
		return service.findById(id, authentication.getName(), administrator);
	}

	@PutMapping("/{id}")
	public LoanResponse update(
			@PathVariable Long id,
			@Valid @RequestBody LoanPayload request,
			Authentication authentication) {
		boolean administrator = isAdministrator(authentication);
		return service.update(id, authentication.getName(), administrator, request.toCommand());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id, Authentication authentication) {
		service.delete(id, authentication.getName(), isAdministrator(authentication));
	}

	@PatchMapping("/{id}/decision")
	public LoanResponse decide(@PathVariable Long id, @Valid @RequestBody LoanDecisionPayload request) {
		return service.decide(id, request.status());
	}

	private boolean isAdministrator(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
	}
}
