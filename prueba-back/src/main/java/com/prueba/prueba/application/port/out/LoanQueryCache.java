package com.prueba.prueba.application.port.out;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.domain.model.Loan;

public interface LoanQueryCache {
	Optional<Loan> findById(Long id);

	void putById(Long id, Loan loan);

	Optional<List<Loan>> findAll();

	void putAll(List<Loan> loans);

	Optional<List<Loan>> findByApplicantId(Long applicantId);

	void putByApplicantId(Long applicantId, List<Loan> loans);

	void invalidateAll();
}
