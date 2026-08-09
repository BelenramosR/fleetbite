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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
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

	protected OrderJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCustomerName() {
		return customerName;
	}

	public void setCustomerName(String customerName) {
		this.customerName = customerName;
	}

	public String getCustomerPhone() {
		return customerPhone;
	}

	public void setCustomerPhone(String customerPhone) {
		this.customerPhone = customerPhone;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public BigDecimal getDeliveryLatitude() {
		return deliveryLatitude;
	}

	public void setDeliveryLatitude(BigDecimal deliveryLatitude) {
		this.deliveryLatitude = deliveryLatitude;
	}

	public BigDecimal getDeliveryLongitude() {
		return deliveryLongitude;
	}

	public void setDeliveryLongitude(BigDecimal deliveryLongitude) {
		this.deliveryLongitude = deliveryLongitude;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public OrderPriority getPriority() {
		return priority;
	}

	public void setPriority(OrderPriority priority) {
		this.priority = priority;
	}

	public OffsetDateTime getPromisedDeliveryAt() {
		return promisedDeliveryAt;
	}

	public void setPromisedDeliveryAt(OffsetDateTime promisedDeliveryAt) {
		this.promisedDeliveryAt = promisedDeliveryAt;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getConfirmedAt() {
		return confirmedAt;
	}

	public void setConfirmedAt(OffsetDateTime confirmedAt) {
		this.confirmedAt = confirmedAt;
	}

	public OffsetDateTime getPreparationStartedAt() {
		return preparationStartedAt;
	}

	public void setPreparationStartedAt(OffsetDateTime preparationStartedAt) {
		this.preparationStartedAt = preparationStartedAt;
	}

	public OffsetDateTime getReadyAt() {
		return readyAt;
	}

	public void setReadyAt(OffsetDateTime readyAt) {
		this.readyAt = readyAt;
	}

	public OffsetDateTime getAssignedAt() {
		return assignedAt;
	}

	public void setAssignedAt(OffsetDateTime assignedAt) {
		this.assignedAt = assignedAt;
	}

	public OffsetDateTime getPickedUpAt() {
		return pickedUpAt;
	}

	public void setPickedUpAt(OffsetDateTime pickedUpAt) {
		this.pickedUpAt = pickedUpAt;
	}

	public OffsetDateTime getInTransitAt() {
		return inTransitAt;
	}

	public void setInTransitAt(OffsetDateTime inTransitAt) {
		this.inTransitAt = inTransitAt;
	}

	public OffsetDateTime getDeliveredAt() {
		return deliveredAt;
	}

	public void setDeliveredAt(OffsetDateTime deliveredAt) {
		this.deliveredAt = deliveredAt;
	}

	public OffsetDateTime getCancelledAt() {
		return cancelledAt;
	}

	public void setCancelledAt(OffsetDateTime cancelledAt) {
		this.cancelledAt = cancelledAt;
	}

	public OffsetDateTime getFailedDeliveryAt() {
		return failedDeliveryAt;
	}

	public void setFailedDeliveryAt(OffsetDateTime failedDeliveryAt) {
		this.failedDeliveryAt = failedDeliveryAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
