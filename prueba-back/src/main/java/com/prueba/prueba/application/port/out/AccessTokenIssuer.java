package com.prueba.prueba.application.port.out;

import com.prueba.prueba.application.dto.AuthResponse;
import com.prueba.prueba.domain.model.UserRole;

public interface AccessTokenIssuer {
	AuthResponse issue(String username, UserRole role);
}
