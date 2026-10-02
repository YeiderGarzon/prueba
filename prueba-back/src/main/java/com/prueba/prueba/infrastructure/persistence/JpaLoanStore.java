package com.prueba.prueba.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.application.port.out.LoanStore;
import com.prueba.prueba.domain.model.Loan;
import com.prueba.prueba.infrastructure.persistence.entity.LoanEntity;
import com.prueba.prueba.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class JpaLoanStore implements LoanStore {
	private final SpringDataLoanRepository loans;
	private final SpringDataUserRepository users;

	JpaLoanStore(SpringDataLoanRepository loans, SpringDataUserRepository users) {
		this.loans = loans;
		this.users = users;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<Loan> findById(Long id) {
		return loans.findById(id).map(LoanEntity::toDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Loan> findAll() {
		return loans.findAllByOrderByCreatedAtDesc().stream().map(LoanEntity::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Loan> findByApplicantId(Long applicantId) {
		return loans.findByApplicant_IdOrderByCreatedAtDesc(applicantId)
				.stream().map(LoanEntity::toDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByApplicantId(Long applicantId) {
		return loans.existsByApplicant_Id(applicantId);
	}

	@Override
	@Transactional
	public Loan save(Loan loan) {
		UserEntity applicant = users.getReferenceById(loan.getApplicantId());
		return loans.save(new LoanEntity(loan, applicant)).toDomain();
	}

	@Override
	@Transactional
	public void delete(Loan loan) {
		loans.deleteById(loan.getId());
	}
}
