package com.fleetbite.order.application.port.out;

import java.util.UUID;

import com.fleetbite.order.domain.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepositoryPort {

	/**
	 * Persists a new order (INSERT). Must not load an existing row first.
	 */
	Order save(Order order);

	/**
	 * Persists changes to an existing order (UPDATE), preserving JPA {@code @Version}.
	 */
	Order update(Order order);

	Optional<Order> findById(UUID id);

	List<Order> findAll();

	void deleteById(UUID id);
}
