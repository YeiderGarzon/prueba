package com.prueba.prueba.infrastructure.cache;

import java.util.List;
import java.util.Optional;

import com.prueba.prueba.application.port.out.LoanQueryCache;
import com.prueba.prueba.domain.model.Loan;
import org.ehcache.Cache;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class EhcacheLoanQueryCache implements LoanQueryCache {
	private static final String ALL_LOANS_KEY = "all";

	private final Cache<Object, Object> byId;
	private final Cache<Object, Object> all;
	private final Cache<Object, Object> byApplicant;

	EhcacheLoanQueryCache(
			Cache<Object, Object> byId,
			Cache<Object, Object> all,
			Cache<Object, Object> byApplicant) {
		this.byId = byId;
		this.all = all;
		this.byApplicant = byApplicant;
	}

	@Override
	public Optional<Loan> findById(Long id) {
		Object cached = byId.get(id);
		return cached instanceof Loan loan ? Optional.of(loan) : Optional.empty();
	}

	@Override
	public void putById(Long id, Loan loan) {
		byId.put(id, loan);
	}

	@Override
	public Optional<List<Loan>> findAll() {
		return findLoanList(all.get(ALL_LOANS_KEY));
	}

	@Override
	public void putAll(List<Loan> loans) {
		all.put(ALL_LOANS_KEY, List.copyOf(loans));
	}

	@Override
	public Optional<List<Loan>> findByApplicantId(Long applicantId) {
		return findLoanList(byApplicant.get(applicantId));
	}

	@Override
	public void putByApplicantId(Long applicantId, List<Loan> loans) {
		byApplicant.put(applicantId, List.copyOf(loans));
	}

	@Override
	public void invalidateAll() {
		clear();
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					clear();
				}
			});
		}
	}

	private Optional<List<Loan>> findLoanList(Object cached) {
		if (cached instanceof List<?> values && values.stream().allMatch(Loan.class::isInstance)) {
			return Optional.of(values.stream().map(Loan.class::cast).toList());
		}
		return Optional.empty();
	}

	private void clear() {
		byId.clear();
		all.clear();
		byApplicant.clear();
	}
}
