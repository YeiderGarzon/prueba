package com.prueba.prueba.infrastructure.security;

import com.prueba.prueba.application.port.out.PasswordHasher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordHasher implements PasswordHasher {
	private final PasswordEncoder encoder;

	public BcryptPasswordHasher(PasswordEncoder encoder) {
		this.encoder = encoder;
	}

	@Override
	public String hash(String rawPassword) {
		return encoder.encode(rawPassword);
	}
}
