package com.prueba.prueba.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.application.port.out.LoanQueryCache;
import com.prueba.prueba.application.port.out.LoanStore;
import com.prueba.prueba.domain.model.Loan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

@Repository
@Primary
class CachedLoanStore implements LoanStore {
	private final LoanStore delegate;
	private final LoanQueryCache cache;

	CachedLoanStore(@Qualifier("jpaLoanStore") LoanStore delegate, LoanQueryCache cache) {
		this.delegate = delegate;
		this.cache = cache;
	}

	@Override
	public Optional<Loan> findById(Long id) {
		Optional<Loan> cached = cache.findById(id);
		if (cached.isPresent()) {
			return cached;
		}
		Optional<Loan> loan = delegate.findById(id);
		loan.ifPresent(value -> cache.putById(id, value));
		return loan;
	}

	@Override
	public List<Loan> findAll() {
		Optional<List<Loan>> cached = cache.findAll();
		if (cached.isPresent()) {
			return cached.get();
		}
		List<Loan> loans = delegate.findAll();
		cache.putAll(loans);
		return loans;
	}

	@Override
	public List<Loan> findByApplicantId(Long applicantId) {
		Optional<List<Loan>> cached = cache.findByApplicantId(applicantId);
		if (cached.isPresent()) {
			return cached.get();
		}
		List<Loan> loans = delegate.findByApplicantId(applicantId);
		cache.putByApplicantId(applicantId, loans);
		return loans;
	}

	@Override
	public boolean existsByApplicantId(Long applicantId) {
		return delegate.existsByApplicantId(applicantId);
	}

	@Override
	public Loan save(Loan loan) {
		Loan saved = delegate.save(loan);
		cache.invalidateAll();
		return saved;
	}

	@Override
	public void delete(Loan loan) {
		delegate.delete(loan);
		cache.invalidateAll();
	}
}
