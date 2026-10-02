package com.prueba.prueba.application.service;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.prueba.prueba.application.exception.ApplicationException;
import com.prueba.prueba.application.exception.ApplicationException.Kind;
import com.prueba.prueba.application.port.in.AuthUseCases;
import com.prueba.prueba.application.port.out.AccessTokenIssuer;
import com.prueba.prueba.application.port.out.CredentialAuthenticator;
import com.prueba.prueba.application.port.out.PasswordHasher;
import com.prueba.prueba.application.port.out.TransactionRunner;
import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.application.dto.AuthResponse;
import com.prueba.prueba.application.dto.LoginRequest;
import com.prueba.prueba.application.dto.RegisterRequest;

public class AuthService implements AuthUseCases {
	private final UserStore users;
	private final PasswordHasher passwordHasher;
	private final CredentialAuthenticator authenticator;
	private final AccessTokenIssuer tokenIssuer;
	private final TransactionRunner transactions;

	public AuthService(
			UserStore users,
			PasswordHasher passwordHasher,
			CredentialAuthenticator authenticator,
			AccessTokenIssuer tokenIssuer,
			TransactionRunner transactions) {
		this.users = users;
		this.passwordHasher = passwordHasher;
		this.authenticator = authenticator;
		this.tokenIssuer = tokenIssuer;
		this.transactions = transactions;
	}

	@Override
	public void register(RegisterRequest request) {
		transactions.required(() -> {
			registerInTransaction(request);
			return null;
		});
	}

	private void registerInTransaction(RegisterRequest request) {
		String username = normalizeUsername(request.username());
		if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ApplicationException(Kind.BAD_REQUEST, "La contraseña excede el máximo permitido");
		}
		if (users.existsByUsername(username)) {
			throw new ApplicationException(Kind.CONFLICT, "El nombre de usuario ya está registrado");
		}
		users.save(new AppUser(
				null,
				username,
				normalizeFullName(request.fullName()),
				passwordHasher.hash(request.password()),
				UserRole.CUSTOMER));
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		CredentialAuthenticator.AuthenticatedUser principal = authenticator.authenticate(
				normalizeUsername(request.username()), request.password());
		UserRole role = principal.roles().contains(UserRole.ADMIN) ? UserRole.ADMIN : UserRole.CUSTOMER;
		return tokenIssuer.issue(principal.username(), role);
	}

	private String normalizeUsername(String username) {
		return username.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeFullName(String fullName) {
		return fullName.trim();
	}
}
