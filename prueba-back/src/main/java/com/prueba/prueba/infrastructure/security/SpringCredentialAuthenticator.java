package com.prueba.prueba.infrastructure.security;

import java.util.List;

import com.prueba.prueba.application.exception.ApplicationException;
import com.prueba.prueba.application.exception.ApplicationException.Kind;
import com.prueba.prueba.application.port.out.CredentialAuthenticator;
import com.prueba.prueba.domain.model.UserRole;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SpringCredentialAuthenticator implements CredentialAuthenticator {
	private final AuthenticationManager authenticationManager;

	public SpringCredentialAuthenticator(AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public AuthenticatedUser authenticate(String username, String password) {
		try {
			var authentication = authenticationManager.authenticate(
					UsernamePasswordAuthenticationToken.unauthenticated(username, password));
			List<UserRole> roles = authentication.getAuthorities().stream()
					.map(GrantedAuthority::getAuthority)
					.filter(authority -> authority.startsWith("ROLE_"))
					.map(authority -> UserRole.valueOf(authority.substring("ROLE_".length())))
					.toList();
			return new AuthenticatedUser(authentication.getName(), roles);
		} catch (AuthenticationException exception) {
			throw new ApplicationException(Kind.UNAUTHORIZED, "Credenciales inválidas");
		}
	}
}
