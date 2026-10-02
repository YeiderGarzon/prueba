package com.prueba.prueba.infrastructure.persistence;

import java.util.function.Supplier;

import com.prueba.prueba.application.port.out.TransactionRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Component
public class SpringTransactionRunner implements TransactionRunner {
	private final TransactionTemplate writeTransaction;
	private final TransactionTemplate readTransaction;

	public SpringTransactionRunner(PlatformTransactionManager transactionManager) {
		writeTransaction = new TransactionTemplate(transactionManager);
		readTransaction = new TransactionTemplate(transactionManager);
		readTransaction.setReadOnly(true);
		readTransaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
	}

	@Override
	public <T> T required(Supplier<T> action) {
		return writeTransaction.execute(status -> action.get());
	}

	@Override
	public <T> T readOnly(Supplier<T> action) {
		return readTransaction.execute(status -> action.get());
	}
}
