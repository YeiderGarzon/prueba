package com.prueba.prueba.application.port.out;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.domain.model.Loan;

public interface LoanStore {
	Optional<Loan> findById(Long id);

	List<Loan> findAll();

	List<Loan> findByApplicantId(Long applicantId);

	boolean existsByApplicantId(Long applicantId);

	Loan save(Loan loan);

	void delete(Loan loan);
}
