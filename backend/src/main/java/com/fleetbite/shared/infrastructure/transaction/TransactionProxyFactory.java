package com.fleetbite.shared.infrastructure.transaction;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Objects;

public final class TransactionProxyFactory {

	private TransactionProxyFactory() {
	}

	public static <T> T readWrite(Class<T> type, T target, PlatformTransactionManager manager) {
		return create(type, target, manager, false, TransactionDefinition.PROPAGATION_REQUIRED);
	}

	public static <T> T readOnly(Class<T> type, T target, PlatformTransactionManager manager) {
		return create(type, target, manager, true, TransactionDefinition.PROPAGATION_REQUIRED);
	}

	public static <T> T requiresNew(Class<T> type, T target, PlatformTransactionManager manager) {
		return create(type, target, manager, false, TransactionDefinition.PROPAGATION_REQUIRES_NEW);
	}

	private static <T> T create(Class<T> type, T target, PlatformTransactionManager manager,
			boolean readOnly, int propagation) {
		Objects.requireNonNull(type, "type is required");
		Objects.requireNonNull(target, "target is required");
		Objects.requireNonNull(manager, "manager is required");

		DefaultTransactionAttribute attribute = new DefaultTransactionAttribute(propagation);
		attribute.setReadOnly(readOnly);
		MatchAlwaysTransactionAttributeSource source = new MatchAlwaysTransactionAttributeSource();
		source.setTransactionAttribute(attribute);
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(type);
		factory.setTarget(target);
		factory.addAdvice(new TransactionInterceptor(manager, source));
		return type.cast(factory.getProxy());
	}
}
