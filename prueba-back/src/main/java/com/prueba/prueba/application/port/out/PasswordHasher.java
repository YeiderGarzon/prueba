package com.prueba.prueba.application.port.out;

public interface PasswordHasher {
	String hash(String rawPassword);
}
