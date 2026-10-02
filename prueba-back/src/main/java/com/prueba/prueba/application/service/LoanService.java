package com.prueba.prueba.application.service;

import java.util.List;

import com.prueba.prueba.application.exception.ApplicationException;
import com.prueba.prueba.application.exception.ApplicationException.Kind;
import com.prueba.prueba.application.port.in.LoanUseCases;
import com.prueba.prueba.application.port.out.LoanStore;
import com.prueba.prueba.application.port.out.TransactionRunner;
import com.prueba.prueba.application.port.out.UserStore;
import com.prueba.prueba.domain.model.AppUser;
import com.prueba.prueba.domain.model.Loan;
import com.prueba.prueba.domain.model.LoanStatus;
import com.prueba.prueba.application.dto.LoanRequest;
import com.prueba.prueba.application.dto.LoanResponse;

public class LoanService implements LoanUseCases {
	private final LoanStore loans;
	private final UserStore users;
	private final TransactionRunner transactions;

	public LoanService(LoanStore loans, UserStore users, TransactionRunner transactions) {
		this.loans = loans;
		this.users = users;
		this.transactions = transactions;
	}

	@Override
	public LoanResponse create(String applicantUsername, LoanRequest request) {
		return transactions.required(() -> createInTransaction(applicantUsername, request));
	}

	private LoanResponse createInTransaction(String applicantUsername, LoanRequest request) {
		AppUser applicant = findApplicant(applicantUsername);
		Loan loan = new Loan(null, applicant.getId(), applicant.getUsername(), applicant.getFullName(),
				request.amount(), request.termMonths(), LoanStatus.PENDING, null);
		return LoanResponse.from(loans.save(loan));
	}

	@Override
	public List<LoanResponse> findAll(String applicantUsername, boolean administrator) {
		return transactions.readOnly(() -> findAllInTransaction(applicantUsername, administrator));
	}

	private List<LoanResponse> findAllInTransaction(String applicantUsername, boolean administrator) {
		List<Loan> result = administrator
				? loans.findAll()
				: loans.findByApplicantId(findApplicant(applicantUsername).getId());
		return result.stream().map(LoanResponse::from).toList();
	}

	@Override
	public LoanResponse findById(Long id, String applicantUsername, boolean administrator) {
		return transactions.readOnly(() -> LoanResponse.from(getLoan(id, applicantUsername, administrator)));
	}

	@Override
	public LoanResponse update(Long id, String applicantUsername, boolean administrator, LoanRequest request) {
		return transactions.required(() -> updateInTransaction(id, applicantUsername, administrator, request));
	}

	private LoanResponse updateInTransaction(
			Long id, String applicantUsername, boolean administrator, LoanRequest request) {
		Loan loan = getLoan(id, applicantUsername, administrator);
		requirePending(loan);
		loan.update(request.amount(), request.termMonths());
		return LoanResponse.from(loans.save(loan));
	}

	@Override
	public void delete(Long id, String applicantUsername, boolean administrator) {
		transactions.required(() -> {
			deleteInTransaction(id, applicantUsername, administrator);
			return null;
		});
	}

	private void deleteInTransaction(Long id, String applicantUsername, boolean administrator) {
		Loan loan = getLoan(id, applicantUsername, administrator);
		requirePending(loan);
		loans.delete(loan);
	}

	@Override
	public LoanResponse decide(Long id, LoanStatus status) {
		return transactions.required(() -> decideInTransaction(id, status));
	}

	private LoanResponse decideInTransaction(Long id, LoanStatus status) {
		if (status != LoanStatus.APPROVED && status != LoanStatus.REJECTED) {
			throw new ApplicationException(Kind.BAD_REQUEST, "La decisión debe ser APPROVED o REJECTED");
		}
		Loan loan = loans.findById(id)
				.orElseThrow(() -> new ApplicationException(Kind.NOT_FOUND, "Préstamo no encontrado"));
		requirePending(loan);
		loan.decide(status);
		return LoanResponse.from(loans.save(loan));
	}

	private Loan getLoan(Long id, String applicantUsername, boolean administrator) {
		Loan loan = loans.findById(id)
				.orElseThrow(() -> new ApplicationException(Kind.NOT_FOUND, "Préstamo no encontrado"));
		if (!administrator && !loan.getApplicantId().equals(findApplicant(applicantUsername).getId())) {
			throw new ApplicationException(Kind.NOT_FOUND, "Préstamo no encontrado");
		}
		return loan;
	}

	private AppUser findApplicant(String username) {
		return users.findByUsername(username)
				.orElseThrow(() -> new ApplicationException(Kind.NOT_FOUND, "Usuario no encontrado"));
	}

	private void requirePending(Loan loan) {
		if (loan.getStatus() != LoanStatus.PENDING) {
			throw new ApplicationException(Kind.CONFLICT, "Solo se pueden modificar o eliminar solicitudes pendientes");
		}
	}
}
