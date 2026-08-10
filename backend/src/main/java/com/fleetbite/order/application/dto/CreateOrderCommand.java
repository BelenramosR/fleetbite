package com.fleetbite.order.application.dto;

import java.math.BigDecimal;

public record CreateOrderCommand(
		String customerName,
		String customerPhone,
		String deliveryAddress,
		double deliveryLatitude,
		double deliveryLongitude,
		BigDecimal totalAmount) {
}
