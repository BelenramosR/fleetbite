package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.OrderHistoryEventType;
import com.fleetbite.order.domain.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_history")
public class OrderHistoryJpaEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "order_id", nullable = false, updatable = false)
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, updatable = false, length = 64)
	private OrderHistoryEventType eventType;

	@Enumerated(EnumType.STRING)
	@Column(name = "previous_status", updatable = false, length = 32)
	private OrderStatus previousStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "new_status", nullable = false, updatable = false, length = 32)
	private OrderStatus newStatus;

	@Column(name = "description", updatable = false, length = 500)
	private String description;

	@Column(name = "performed_by", updatable = false)
	private UUID performedBy;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected OrderHistoryJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	public OrderHistoryEventType getEventType() {
		return eventType;
	}

	public void setEventType(OrderHistoryEventType eventType) {
		this.eventType = eventType;
	}

	public OrderStatus getPreviousStatus() {
		return previousStatus;
	}

	public void setPreviousStatus(OrderStatus previousStatus) {
		this.previousStatus = previousStatus;
	}

	public OrderStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(OrderStatus newStatus) {
		this.newStatus = newStatus;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public UUID getPerformedBy() {
		return performedBy;
	}

	public void setPerformedBy(UUID performedBy) {
		this.performedBy = performedBy;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
