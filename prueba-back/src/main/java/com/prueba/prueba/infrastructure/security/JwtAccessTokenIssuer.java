package com.prueba.prueba.infrastructure.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.prueba.prueba.application.port.out.AccessTokenIssuer;
import com.prueba.prueba.domain.model.UserRole;
import com.prueba.prueba.application.dto.AuthResponse;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokenIssuer implements AccessTokenIssuer {
	private static final Duration TOKEN_LIFETIME = Duration.ofMinutes(30);
	private final JwtEncoder jwtEncoder;

	public JwtAccessTokenIssuer(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	@Override
	public AuthResponse issue(String username, UserRole role) {
		Instant issuedAt = Instant.now();
		List<String> roles = List.of("ROLE_" + role.name());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer("loan-api")
				.issuedAt(issuedAt)
				.expiresAt(issuedAt.plus(TOKEN_LIFETIME))
				.subject(username)
				.claim("roles", roles)
				.build();
		JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
		return new AuthResponse(token, "Bearer", TOKEN_LIFETIME.toSeconds(), username, role);
	}
}
