package com.fleetbite.delivery.infrastructure.transaction;

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

class DeliveryTransactionProxyFactoryTest {

	@Test
	void readWriteCommitsRequiredTransaction() {
		var manager = managerWithStatus();
		TestOperation operation = DeliveryTransactionProxyFactory.readWrite(
				TestOperation.class, value -> value, manager);
		assertEquals("assignment", operation.execute("assignment"));
		var captor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(manager).getTransaction(captor.capture());
		assertFalse(captor.getValue().isReadOnly());
		assertEquals(TransactionDefinition.PROPAGATION_REQUIRED, captor.getValue().getPropagationBehavior());
	}

	@Test
	void readOnlyMarksRequiredTransaction() {
		var manager = managerWithStatus();
		TestOperation operation = DeliveryTransactionProxyFactory.readOnly(
				TestOperation.class, value -> value, manager);
		operation.execute("assignment");
		var captor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(manager).getTransaction(captor.capture());
		assertTrue(captor.getValue().isReadOnly());
	}

	@Test
	void autoAssignmentUsesRequiresNew() {
		var manager = managerWithStatus();
		TestOperation operation = DeliveryTransactionProxyFactory.requiresNew(
				TestOperation.class, value -> value, manager);
		operation.execute("assignment");
		var captor = org.mockito.ArgumentCaptor.forClass(TransactionDefinition.class);
		verify(manager).getTransaction(captor.capture());
		assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW, captor.getValue().getPropagationBehavior());
	}

	@Test
	void rollsBackOnFailure() {
		PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(manager.getTransaction(any())).thenReturn(status);
		TestOperation operation = DeliveryTransactionProxyFactory.readWrite(
				TestOperation.class,
				value -> { throw new IllegalStateException("failure"); },
				manager);
		assertThrows(IllegalStateException.class, () -> operation.execute("assignment"));
		verify(manager).rollback(status);
	}

	private static PlatformTransactionManager managerWithStatus() {
		PlatformTransactionManager manager = mock(PlatformTransactionManager.class);
		TransactionStatus status = new SimpleTransactionStatus();
		when(manager.getTransaction(any())).thenReturn(status);
		return manager;
	}

	private interface TestOperation {
		String execute(String value);
	}
}
