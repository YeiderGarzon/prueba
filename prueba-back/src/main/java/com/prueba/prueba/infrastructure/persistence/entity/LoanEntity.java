package com.prueba.prueba.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.prueba.prueba.domain.model.Loan;
import com.prueba.prueba.domain.model.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class LoanEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "applicant_id", nullable = false)
	private UserEntity applicant;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@Column(nullable = false)
	private Integer termMonths;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private LoanStatus status;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	protected LoanEntity() {
	}

	public LoanEntity(Loan loan, UserEntity applicant) {
		this.id = loan.getId();
		this.applicant = applicant;
		this.amount = loan.getAmount();
		this.termMonths = loan.getTermMonths();
		this.status = loan.getStatus();
		this.createdAt = loan.getCreatedAt();
	}

	@PrePersist
	void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (status == null) {
			status = LoanStatus.PENDING;
		}
	}

	public Long getId() {
		return id;
	}

	public UserEntity getApplicant() {
		return applicant;
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

	public Loan toDomain() {
		return new Loan(
				id,
				applicant.getId(),
				applicant.getUsername(),
				applicant.getFullName(),
				amount,
				termMonths,
				status,
				createdAt);
	}
}
