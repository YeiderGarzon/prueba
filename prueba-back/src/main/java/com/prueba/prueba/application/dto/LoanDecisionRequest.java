package com.prueba.prueba.application.dto;

import com.prueba.prueba.domain.model.LoanStatus;

public record LoanDecisionRequest(LoanStatus status) {
}
