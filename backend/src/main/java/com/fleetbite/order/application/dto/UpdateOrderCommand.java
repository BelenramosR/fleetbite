package com.fleetbite.order.application.dto;

import java.math.BigDecimal;
import java.util.Objects;

public final class UpdateOrderCommand {

	private final String customerName;
	private final String customerPhone;
	private final String deliveryAddress;
	private final double deliveryLatitude;
	private final double deliveryLongitude;
	private final BigDecimal totalAmount;

	public UpdateOrderCommand(
			String customerName,
			String customerPhone,
			String deliveryAddress,
			double deliveryLatitude,
			double deliveryLongitude,
			BigDecimal totalAmount) {
		this.customerName = customerName;
		this.customerPhone = customerPhone;
		this.deliveryAddress = deliveryAddress;
		this.deliveryLatitude = deliveryLatitude;
		this.deliveryLongitude = deliveryLongitude;
		this.totalAmount = totalAmount;
	}

	public String customerName() {
		return customerName;
	}

	public String customerPhone() {
		return customerPhone;
	}

	public String deliveryAddress() {
		return deliveryAddress;
	}

	public double deliveryLatitude() {
		return deliveryLatitude;
	}

	public double deliveryLongitude() {
		return deliveryLongitude;
	}

	public BigDecimal totalAmount() {
		return totalAmount;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof UpdateOrderCommand that)) {
			return false;
		}
		return Double.compare(deliveryLatitude, that.deliveryLatitude) == 0
				&& Double.compare(deliveryLongitude, that.deliveryLongitude) == 0
				&& Objects.equals(customerName, that.customerName)
				&& Objects.equals(customerPhone, that.customerPhone)
				&& Objects.equals(deliveryAddress, that.deliveryAddress)
				&& Objects.equals(totalAmount, that.totalAmount);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				customerName,
				customerPhone,
				deliveryAddress,
				deliveryLatitude,
				deliveryLongitude,
				totalAmount);
	}
}
