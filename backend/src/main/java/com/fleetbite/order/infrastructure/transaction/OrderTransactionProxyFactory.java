package com.fleetbite.order.infrastructure.transaction;

import org.springframework.aop.framework.ProxyFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Objects;

public final class OrderTransactionProxyFactory {

	private OrderTransactionProxyFactory() {
	}

	public static <T> T readWrite(
			Class<T> useCaseType,
			T target,
			PlatformTransactionManager transactionManager) {
		return createProxy(useCaseType, target, transactionManager, false);
	}

	public static <T> T readOnly(
			Class<T> useCaseType,
			T target,
			PlatformTransactionManager transactionManager) {
		return createProxy(useCaseType, target, transactionManager, true);
	}

	private static <T> T createProxy(
			Class<T> useCaseType,
			T target,
			PlatformTransactionManager transactionManager,
			boolean readOnly) {
		Objects.requireNonNull(useCaseType, "useCaseType is required");
		Objects.requireNonNull(target, "target is required");
		Objects.requireNonNull(transactionManager, "transactionManager is required");

		DefaultTransactionAttribute transactionAttribute = new DefaultTransactionAttribute();
		transactionAttribute.setReadOnly(readOnly);

		MatchAlwaysTransactionAttributeSource attributeSource =
				new MatchAlwaysTransactionAttributeSource();
		attributeSource.setTransactionAttribute(transactionAttribute);

		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setInterfaces(useCaseType);
		proxyFactory.setTarget(target);
		proxyFactory.addAdvice(new TransactionInterceptor(transactionManager, attributeSource));

		return useCaseType.cast(proxyFactory.getProxy());
	}
}
