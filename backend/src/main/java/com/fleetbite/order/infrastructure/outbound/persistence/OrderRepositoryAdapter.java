package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;
import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class OrderRepositoryAdapter implements OrderRepositoryPort {

	private final SpringDataOrderRepository springDataOrderRepository;
	private final OrderPersistenceMapper orderPersistenceMapper;

	public OrderRepositoryAdapter(
			SpringDataOrderRepository springDataOrderRepository,
			OrderPersistenceMapper orderPersistenceMapper) {
		this.springDataOrderRepository = Objects.requireNonNull(springDataOrderRepository);
		this.orderPersistenceMapper = Objects.requireNonNull(orderPersistenceMapper);
	}

	/**
	 * CREATE path: new entity with {@code version == null}. No SELECT.
	 */
	@Override
	public Order save(Order order) {
		Objects.requireNonNull(order, "order is required");
		OrderJpaEntity entity = orderPersistenceMapper.toEntity(order);
		OrderJpaEntity saved = springDataOrderRepository.save(entity);
		return orderPersistenceMapper.toDomain(saved);
	}

	/**
	 * UPDATE path: load existing entity to preserve {@code @Version}, then copy domain state.
	 * The extra SELECT is justified solely by optimistic locking without version in the domain.
	 */
	@Override
	public Order update(Order order) {
		Objects.requireNonNull(order, "order is required");
		OrderJpaEntity existing = springDataOrderRepository.findById(order.id().value())
				.orElseThrow(() -> new ResourceNotFoundException("Order", order.id().value()));
		orderPersistenceMapper.copyToEntity(order, existing);
		OrderJpaEntity saved = springDataOrderRepository.save(existing);
		return orderPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Order> findById(OrderId id) {
		Objects.requireNonNull(id, "id is required");
		return springDataOrderRepository.findById(id.value())
				.map(orderPersistenceMapper::toDomain);
	}

	@Override
	public List<Order> findAll() {
		return springDataOrderRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt")).stream()
				.map(orderPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public void deleteById(OrderId id) {
		Objects.requireNonNull(id, "id is required");
		if (!springDataOrderRepository.existsById(id.value())) {
			throw new ResourceNotFoundException("Order", id.value());
		}
		springDataOrderRepository.deleteById(id.value());
	}
}
