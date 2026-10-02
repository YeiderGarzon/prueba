package com.prueba.prueba.infrastructure.config;

import com.prueba.prueba.application.port.in.AuthUseCases;
import com.prueba.prueba.application.port.in.LoanUseCases;
import com.prueba.prueba.application.port.in.UserUseCases;
import com.prueba.prueba.application.port.out.AccessTokenIssuer;
import com.prueba.prueba.application.port.out.CredentialAuthenticator;
import com.prueba.prueba.application.port.out.LoanStore;
import com.prueba.prueba.application.port.out.LoanQueryCache;
import com.prueba.prueba.application.port.out.PasswordHasher;
import com.prueba.prueba.application.port.out.TransactionRunner;
import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.application.service.AuthService;
import com.prueba.prueba.application.service.LoanService;
import com.prueba.prueba.application.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {
	@Bean
	LoanUseCases loanUseCases(LoanStore loans, UserStore users, TransactionRunner transactions) {
		return new LoanService(loans, users, transactions);
	}

	@Bean
	UserUseCases userUseCases(
			UserStore users,
			LoanStore loans,
			PasswordHasher passwordHasher,
			TransactionRunner transactions,
			LoanQueryCache loanQueryCache) {
		return new UserService(users, loans, passwordHasher, transactions, loanQueryCache);
	}

	@Bean
	AuthUseCases authUseCases(
			UserStore users,
			PasswordHasher passwordHasher,
			CredentialAuthenticator authenticator,
			AccessTokenIssuer tokenIssuer,
			TransactionRunner transactions) {
		return new AuthService(users, passwordHasher, authenticator, tokenIssuer, transactions);
	}
}
