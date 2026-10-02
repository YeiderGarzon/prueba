package com.prueba.prueba.application.port.in;

import java.util.List;

import com.prueba.prueba.application.dto.LoanRequest;
import com.prueba.prueba.application.dto.LoanResponse;
import com.prueba.prueba.domain.model.LoanStatus;

public interface LoanUseCases {
	LoanResponse create(String applicantUsername, LoanRequest request);

	List<LoanResponse> findAll(String applicantUsername, boolean administrator);

	LoanResponse findById(Long id, String applicantUsername, boolean administrator);

	LoanResponse update(Long id, String applicantUsername, boolean administrator, LoanRequest request);

	void delete(Long id, String applicantUsername, boolean administrator);

	LoanResponse decide(Long id, LoanStatus status);
}
