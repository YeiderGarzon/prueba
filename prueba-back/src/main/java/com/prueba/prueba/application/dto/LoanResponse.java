package com.prueba.prueba.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prueba.prueba.domain.model.Loan;
import com.prueba.prueba.domain.model.LoanStatus;

public record LoanResponse(
		Long id,
		Long applicantId,
		String applicantUsername,
		String applicantFullName,
		BigDecimal amount,
		Integer termMonths,
		LoanStatus status,
		LocalDateTime createdAt) {
	public static LoanResponse from(Loan loan) {
		return new LoanResponse(
				loan.getId(),
				loan.getApplicantId(),
				loan.getApplicantUsername(),
				loan.getApplicantFullName(),
				loan.getAmount(),
				loan.getTermMonths(),
				loan.getStatus(),
				loan.getCreatedAt());
	}
}
