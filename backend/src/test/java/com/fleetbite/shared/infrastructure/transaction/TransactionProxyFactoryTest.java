package com.fleetbite.shared.infrastructure.transaction;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionProxyFactoryTest {

	@Test
	void readOnlyCommitsWithReadOnlyAttribute() {
		PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(manager.getTransaction(any())).thenReturn(status);
		Operation operation = TransactionProxyFactory.readOnly(Operation.class, value -> value, manager);

		assertEquals("value", operation.execute("value"));
		var captor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(manager).getTransaction(captor.capture());
		assertTrue(captor.getValue().isReadOnly());
		verify(manager).commit(status);
	}

	@Test
	void readWriteRollsBackOnFailure() {
		PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(manager.getTransaction(any())).thenReturn(status);
		Operation operation = TransactionProxyFactory.readWrite(
				Operation.class, value -> { throw new IllegalStateException("failure"); }, manager);

		assertThrows(IllegalStateException.class, () -> operation.execute("value"));
		verify(manager).rollback(status);
	}

	private interface Operation {
		String execute(String value);
	}
}
