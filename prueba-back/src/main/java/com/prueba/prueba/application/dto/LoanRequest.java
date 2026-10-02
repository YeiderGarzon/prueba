package com.prueba.prueba.application.dto;

import java.math.BigDecimal;

public record LoanRequest(BigDecimal amount, Integer termMonths) {
}
