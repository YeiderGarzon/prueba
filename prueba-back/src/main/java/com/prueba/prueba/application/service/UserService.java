package com.prueba.prueba.application.service;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import com.prueba.prueba.application.exception.ApplicationException;
import com.prueba.prueba.application.exception.ApplicationException.Kind;
import com.prueba.prueba.application.port.in.UserUseCases;
import com.prueba.prueba.application.port.out.LoanStore;
import com.prueba.prueba.application.port.out.LoanQueryCache;
import com.prueba.prueba.application.port.out.PasswordHasher;
import com.prueba.prueba.application.port.out.TransactionRunner;
import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.application.dto.UserRequest;
import com.prueba.prueba.application.dto.UserResponse;
import com.prueba.prueba.application.dto.UserProfileUpdateRequest;
import com.prueba.prueba.application.dto.UserUpdateRequest;

public class UserService implements UserUseCases {
	private final UserStore users;
	private final LoanStore loans;
	private final PasswordHasher passwordHasher;
	private final TransactionRunner transactions;
	private final LoanQueryCache loanQueryCache;

	public UserService(
			UserStore users,
			LoanStore loans,
			PasswordHasher passwordHasher,
			TransactionRunner transactions,
			LoanQueryCache loanQueryCache) {
		this.users = users;
		this.loans = loans;
		this.passwordHasher = passwordHasher;
		this.transactions = transactions;
		this.loanQueryCache = loanQueryCache;
	}

	@Override
	public List<UserResponse> findAll() {
		return transactions.readOnly(() -> users.findAll().stream().map(this::toResponse).toList());
	}

	@Override
	public UserResponse findById(Long id) {
		return transactions.readOnly(() -> toResponse(getUser(id)));
	}

	@Override
	public UserResponse findCurrent(String username) {
		return transactions.readOnly(() -> users.findByUsername(username)
				.map(this::toResponse)
				.orElseThrow(() -> new ApplicationException(Kind.NOT_FOUND, "Usuario no encontrado")));
	}

	@Override
	public UserResponse updateCurrent(String currentUsername, UserProfileUpdateRequest request) {
		UserResponse updated = transactions.required(() -> {
			AppUser user = users.findByUsername(currentUsername)
					.orElseThrow(() -> new ApplicationException(Kind.NOT_FOUND, "Usuario no encontrado"));
			String username = normalizeUsername(request.username());
			if (users.existsByUsernameAndIdNot(username, user.getId())) {
				throw new ApplicationException(Kind.CONFLICT, "El nombre de usuario ya está registrado");
			}
			String fullName = normalizeFullName(request.fullName());
			String passwordHash = null;
			if (request.password() != null && !request.password().isBlank()) {
				validatePasswordLength(request.password());
				passwordHash = passwordHasher.hash(request.password());
			}
			user.update(username, fullName, user.getRole(), passwordHash);
			return toResponse(users.save(user));
		});
		loanQueryCache.invalidateAll();
		return updated;
	}

	@Override
	public UserResponse create(UserRequest request) {
		return transactions.required(() -> createInTransaction(request));
	}

	private UserResponse createInTransaction(UserRequest request) {
		String username = normalizeUsername(request.username());
		String fullName = normalizeFullName(request.fullName());
		validatePasswordLength(request.password());
		if (users.existsByUsername(username)) {
			throw new ApplicationException(Kind.CONFLICT, "El nombre de usuario ya está registrado");
		}
		return toResponse(users.save(new AppUser(
				null, username, fullName, passwordHasher.hash(request.password()), request.role())));
	}

	@Override
	public UserResponse update(Long id, UserUpdateRequest request) {
		UserResponse updated = transactions.required(() -> updateInTransaction(id, request));
		loanQueryCache.invalidateAll();
		return updated;
	}

	private UserResponse updateInTransaction(Long id, UserUpdateRequest request) {
		AppUser user = getUser(id);
		String username = normalizeUsername(request.username());
		String fullName = normalizeFullName(request.fullName());
		if (users.existsByUsernameAndIdNot(username, id)) {
			throw new ApplicationException(Kind.CONFLICT, "El nombre de usuario ya está registrado");
		}
		if (user.getRole() == UserRole.ADMIN
				&& request.role() != UserRole.ADMIN
				&& users.countByRole(UserRole.ADMIN) <= 1) {
			throw new ApplicationException(Kind.CONFLICT, "No se puede quitar el último administrador");
		}
		String passwordHash = null;
		if (request.password() != null && !request.password().isBlank()) {
			validatePasswordLength(request.password());
			passwordHash = passwordHasher.hash(request.password());
		}
		user.update(username, fullName, request.role(), passwordHash);
		return toResponse(users.save(user));
	}

	@Override
	public void delete(Long id, String currentUsername) {
		transactions.required(() -> {
			deleteInTransaction(id, currentUsername);
			return null;
		});
	}

	private void deleteInTransaction(Long id, String currentUsername) {
		AppUser user = getUser(id);
		if (user.getUsername().equalsIgnoreCase(currentUsername)) {
			throw new ApplicationException(Kind.CONFLICT, "No se puede eliminar el usuario autenticado");
		}
		if (user.getRole() == UserRole.ADMIN && users.countByRole(UserRole.ADMIN) <= 1) {
			throw new ApplicationException(Kind.CONFLICT, "No se puede eliminar el último administrador");
		}
		if (loans.existsByApplicantId(user.getId())) {
			throw new ApplicationException(Kind.CONFLICT, "No se puede eliminar un usuario con solicitudes de préstamo");
		}
		users.delete(user);
	}

	private AppUser getUser(Long id) {
		return users.findById(id)
				.orElseThrow(() -> new ApplicationException(Kind.NOT_FOUND, "Usuario no encontrado"));
	}

	private UserResponse toResponse(AppUser user) {
		return new UserResponse(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
	}

	private String normalizeUsername(String username) {
		return username.trim().toLowerCase(Locale.ROOT);
	}

	private String normalizeFullName(String fullName) {
		return fullName.trim();
	}

	private void validatePasswordLength(String password) {
		if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new ApplicationException(Kind.BAD_REQUEST, "La contraseña excede el máximo permitido");
		}
	}
}
