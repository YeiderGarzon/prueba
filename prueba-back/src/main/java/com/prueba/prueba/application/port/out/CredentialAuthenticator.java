package com.prueba.prueba.application.port.out;

import java.util.List;

import com.prueba.prueba.domain.model.UserRole;

public interface CredentialAuthenticator {
	AuthenticatedUser authenticate(String username, String password);

	record AuthenticatedUser(String username, List<UserRole> roles) {
	}
}
