package com.fleetbite.driver.application.dto;

public record CreateDriverCommand(
		String name,
		String phone,
		Double currentLatitude,
		Double currentLongitude) {
}
