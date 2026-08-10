package com.fleetbite.order.application.dto;

import java.math.BigDecimal;

public record UpdateOrderCommand(
		String customerName,
		String customerPhone,
		String deliveryAddress,
		double deliveryLatitude,
		double deliveryLongitude,
		BigDecimal totalAmount) {
}
