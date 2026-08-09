package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.application.port.out.OrderRepositoryPort;
import com.fleetbite.order.domain.model.Order;
import com.fleetbite.order.domain.model.OrderId;
import org.springframework.stereotype.Component;

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

	@Override
	public Order save(Order order) {
		OrderJpaEntity entity = orderPersistenceMapper.toEntity(order);
		OrderJpaEntity saved = springDataOrderRepository.save(entity);
		return orderPersistenceMapper.toDomain(saved);
	}

	@Override
	public Optional<Order> findById(OrderId id) {
		Objects.requireNonNull(id, "id is required");
		return springDataOrderRepository.findById(id.value())
				.map(orderPersistenceMapper::toDomain);
	}
}
