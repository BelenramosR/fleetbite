package com.fleetbite.driver.application.dto;

import java.util.UUID;

public record CreateDriverCommand(
		UUID userId,
		String phone,
		Double currentLatitude,
		Double currentLongitude) {
}
