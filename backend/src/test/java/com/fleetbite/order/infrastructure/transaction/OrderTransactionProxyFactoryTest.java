package com.fleetbite.order.infrastructure.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderTransactionProxyFactoryTest {

	@Test
	void createsReadWriteTransactionAndCommits() {
		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(transactionManager.getTransaction(any())).thenReturn(status);

		TestOperation operation = OrderTransactionProxyFactory.readWrite(
				TestOperation.class,
				value -> "processed-" + value,
				transactionManager);

		assertEquals("processed-order", operation.execute("order"));
		var definitionCaptor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(transactionManager).getTransaction(definitionCaptor.capture());
		assertFalse(definitionCaptor.getValue().isReadOnly());
		verify(transactionManager).commit(status);
	}

	@Test
	void createsReadOnlyTransactionAndCommits() {
		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(transactionManager.getTransaction(any())).thenReturn(status);

		TestOperation operation = OrderTransactionProxyFactory.readOnly(
				TestOperation.class,
				value -> value,
				transactionManager);

		assertEquals("order", operation.execute("order"));
		var definitionCaptor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(transactionManager).getTransaction(definitionCaptor.capture());
		assertTrue(definitionCaptor.getValue().isReadOnly());
		verify(transactionManager).commit(status);
	}

	@Test
	void rollsBackWhenUseCaseFails() {
		PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(transactionManager.getTransaction(any())).thenReturn(status);

		TestOperation operation = OrderTransactionProxyFactory.readWrite(
				TestOperation.class,
				value -> {
					throw new IllegalStateException("failure");
				},
				transactionManager);

		assertThrows(IllegalStateException.class, () -> operation.execute("order"));
		verify(transactionManager).rollback(status);
	}

	private interface TestOperation {

		String execute(String value);
	}
}
