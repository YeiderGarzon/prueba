package com.prueba.prueba.infrastructure.web.dto;

import com.prueba.prueba.application.dto.LoanDecisionRequest;
import com.prueba.prueba.domain.model.LoanStatus;
import jakarta.validation.constraints.NotNull;

public record LoanDecisionPayload(@NotNull LoanStatus status) {
	public LoanDecisionRequest toCommand() {
		return new LoanDecisionRequest(status);
	}
}
