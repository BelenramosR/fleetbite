package com.fleetbite.driver.infrastructure.outbound.persistence;

import com.fleetbite.driver.domain.model.DriverStatus;
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
@Table(name = "drivers")
public class DriverJpaEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "user_id", nullable = false, unique = true)
	private UUID userId;

	@Column(name = "phone", unique = true, length = 32)
	private String phone;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 16)
	private DriverStatus status;

	@Column(name = "current_latitude", precision = 10, scale = 7)
	private BigDecimal currentLatitude;

	@Column(name = "current_longitude", precision = 10, scale = 7)
	private BigDecimal currentLongitude;

	@Column(name = "vehicle_id", unique = true)
	private UUID vehicleId;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Version
	@Column(name = "version", nullable = false)
	private Long version;

	protected DriverJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public DriverStatus getStatus() {
		return status;
	}

	public void setStatus(DriverStatus status) {
		this.status = status;
	}

	public BigDecimal getCurrentLatitude() {
		return currentLatitude;
	}

	public void setCurrentLatitude(BigDecimal currentLatitude) {
		this.currentLatitude = currentLatitude;
	}

	public BigDecimal getCurrentLongitude() {
		return currentLongitude;
	}

	public void setCurrentLongitude(BigDecimal currentLongitude) {
		this.currentLongitude = currentLongitude;
	}

	public UUID getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(UUID vehicleId) {
		this.vehicleId = vehicleId;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(OffsetDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(OffsetDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}
}
