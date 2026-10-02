package com.prueba.prueba.infrastructure.persistence;

import java.util.List;

import com.prueba.prueba.infrastructure.persistence.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLoanRepository extends JpaRepository<LoanEntity, Long> {
	List<LoanEntity> findByApplicant_IdOrderByCreatedAtDesc(Long applicantId);

	List<LoanEntity> findAllByOrderByCreatedAtDesc();

	boolean existsByApplicant_Id(Long applicantId);
}
