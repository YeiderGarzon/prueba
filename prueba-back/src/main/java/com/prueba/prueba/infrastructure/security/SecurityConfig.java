package com.prueba.prueba.infrastructure.security;

import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
	@Bean
	SecretKey jwtSigningKey(@Value("${app.security.jwt-secret:}") String secret) {
		byte[] key = secret.getBytes(StandardCharsets.UTF_8);
		if (key.length < 32) {
			throw new IllegalStateException("APP_JWT_SECRET debe contener al menos 32 bytes");
		}
		return new SecretKeySpec(key, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey jwtSigningKey) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSigningKey.getEncoded()));
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey jwtSigningKey) {
		NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSigningKey)
				.macAlgorithm(MacAlgorithm.HS256)
				.build();
		decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("loan-api"));
		return decoder;
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(
			UserDetailsService userDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return new ProviderManager(provider);
	}

	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter(UserDetailsService userDetailsService) {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			List<GrantedAuthority> authorities = new ArrayList<>();
			userDetailsService.loadUserByUsername(jwt.getSubject())
					.getAuthorities()
					.forEach(authorities::add);
			return authorities;
		});
		return converter;
	}

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			JwtAuthenticationConverter jwtAuthenticationConverter,
			ObjectMapper objectMapper) throws Exception {
		http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) -> writeApiError(
								objectMapper,
								response,
								HttpStatus.UNAUTHORIZED,
								"Se requiere autenticación válida para acceder a este recurso."))
						.accessDeniedHandler((request, response, exception) -> writeApiError(
								objectMapper,
								response,
								HttpStatus.FORBIDDEN,
								"No tienes permisos para realizar esta operación.")))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.POST, "/api/loans").hasRole("CUSTOMER")
						.requestMatchers(HttpMethod.PATCH, "/api/loans/*/decision").hasRole("ADMIN")
						.requestMatchers("/api/users/me", "/api/users/me/**").authenticated()
						.requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")
						.requestMatchers("/api/auth/me", "/api/loans", "/api/loans/**").authenticated()
						.anyRequest().denyAll())
				.oauth2ResourceServer(resourceServer -> resourceServer
						.authenticationEntryPoint((request, response, exception) -> writeApiError(
								objectMapper,
								response,
								HttpStatus.UNAUTHORIZED,
								"El token de acceso no es válido o ha expirado."))
						.accessDeniedHandler((request, response, exception) -> writeApiError(
								objectMapper,
								response,
								HttpStatus.FORBIDDEN,
								"No tienes permisos para realizar esta operación."))
						.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));
		return http.build();
	}

	private static void writeApiError(
			ObjectMapper objectMapper,
			HttpServletResponse response,
			HttpStatus status,
			String detail) throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		if (status == HttpStatus.UNAUTHORIZED) {
			response.setHeader("WWW-Authenticate", "Bearer");
		}
		objectMapper.writeValue(response.getOutputStream(), Map.of(
				"status", status.value(),
				"error", status.getReasonPhrase(),
				"message", detail,
				"detail", detail));
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(java.util.List.of(
				"http://localhost:4200",
				"http://127.0.0.1:4200"));
		configuration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type"));
		configuration.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}
