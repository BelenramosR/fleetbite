package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.application.port.out.OrderHistoryRepositoryPort;
import com.fleetbite.order.domain.model.OrderHistoryEvent;
import com.fleetbite.order.domain.model.OrderId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class OrderHistoryRepositoryAdapter implements OrderHistoryRepositoryPort {

	private final SpringDataOrderHistoryRepository springDataOrderHistoryRepository;
	private final OrderHistoryPersistenceMapper orderHistoryPersistenceMapper;

	public OrderHistoryRepositoryAdapter(
			SpringDataOrderHistoryRepository springDataOrderHistoryRepository,
			OrderHistoryPersistenceMapper orderHistoryPersistenceMapper) {
		this.springDataOrderHistoryRepository = Objects.requireNonNull(springDataOrderHistoryRepository);
		this.orderHistoryPersistenceMapper = Objects.requireNonNull(orderHistoryPersistenceMapper);
	}

	@Override
	public void save(OrderHistoryEvent event) {
		Objects.requireNonNull(event, "event is required");
		springDataOrderHistoryRepository.save(orderHistoryPersistenceMapper.toEntity(event));
	}

	@Override
	public List<OrderHistoryEvent> findByOrderId(OrderId orderId) {
		Objects.requireNonNull(orderId, "orderId is required");
		return springDataOrderHistoryRepository.findByOrderIdOrderByCreatedAtAsc(orderId.value()).stream()
				.map(orderHistoryPersistenceMapper::toDomain)
				.toList();
	}
}
