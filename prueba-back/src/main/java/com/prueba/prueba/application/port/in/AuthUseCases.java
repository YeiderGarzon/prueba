package com.prueba.prueba.application.port.in;

import com.prueba.prueba.application.dto.AuthResponse;
import com.prueba.prueba.application.dto.LoginRequest;
import com.prueba.prueba.application.dto.RegisterRequest;

public interface AuthUseCases {
	void register(RegisterRequest request);

	AuthResponse login(LoginRequest request);
}
