package com.prueba.prueba.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Loan {
	private final Long id;
	private final Long applicantId;
	private final String applicantUsername;
	private final String applicantFullName;
	private final LocalDateTime createdAt;
	private BigDecimal amount;
	private Integer termMonths;
	private LoanStatus status;

	public Loan(
			Long id,
			Long applicantId,
			String applicantUsername,
			String applicantFullName,
			BigDecimal amount,
			Integer termMonths,
			LoanStatus status,
			LocalDateTime createdAt) {
		this.id = id;
		this.applicantId = applicantId;
		this.applicantUsername = applicantUsername;
		this.applicantFullName = applicantFullName;
		this.amount = amount;
		this.termMonths = termMonths;
		this.status = status;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public Long getApplicantId() {
		return applicantId;
	}

	public String getApplicantUsername() {
		return applicantUsername;
	}

	public String getApplicantFullName() {
		return applicantFullName;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Integer getTermMonths() {
		return termMonths;
	}

	public LoanStatus getStatus() {
		return status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void decide(LoanStatus status) {
		this.status = status;
	}

	public void update(BigDecimal amount, Integer termMonths) {
		this.amount = amount;
		this.termMonths = termMonths;
	}
}
