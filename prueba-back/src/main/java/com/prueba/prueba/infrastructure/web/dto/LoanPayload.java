package com.prueba.prueba.infrastructure.web.dto;

import java.math.BigDecimal;

import com.prueba.prueba.application.dto.LoanRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LoanPayload(
		@NotNull @DecimalMin("100.00") @Digits(integer = 10, fraction = 2) BigDecimal amount,
		@NotNull @Positive Integer termMonths) {
	public LoanRequest toCommand() {
		return new LoanRequest(amount, termMonths);
	}
}
