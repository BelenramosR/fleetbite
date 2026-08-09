package com.fleetbite.order.infrastructure.outbound.persistence;

import com.fleetbite.order.domain.model.OrderPriority;
import com.fleetbite.order.domain.model.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderJpaEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "code", nullable = false, unique = true, length = 40)
	private String code;

	@Column(name = "customer_name", nullable = false, length = 120)
	private String customerName;

	@Column(name = "customer_phone", nullable = false, length = 32)
	private String customerPhone;

	@Column(name = "delivery_address", nullable = false, length = 255)
	private String deliveryAddress;

	@Column(name = "delivery_latitude", nullable = false, precision = 10, scale = 7)
	private BigDecimal deliveryLatitude;

	@Column(name = "delivery_longitude", nullable = false, precision = 10, scale = 7)
	private BigDecimal deliveryLongitude;

	@Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
	private BigDecimal totalAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 32)
	private OrderStatus status;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", nullable = false, length = 16)
	private OrderPriority priority;

	@Column(name = "promised_delivery_at", nullable = false)
	private OffsetDateTime promisedDeliveryAt;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "confirmed_at")
	private OffsetDateTime confirmedAt;

	@Column(name = "preparation_started_at")
	private OffsetDateTime preparationStartedAt;

	@Column(name = "ready_at")
	private OffsetDateTime readyAt;

	@Column(name = "assigned_at")
	private OffsetDateTime assignedAt;

	@Column(name = "picked_up_at")
	private OffsetDateTime pickedUpAt;

	@Column(name = "in_transit_at")
	private OffsetDateTime inTransitAt;

	@Column(name = "delivered_at")
	private OffsetDateTime deliveredAt;

	@Column(name = "cancelled_at")
	private OffsetDateTime cancelledAt;

	@Column(name = "failed_delivery_at")
	private OffsetDateTime failedDeliveryAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;
}
