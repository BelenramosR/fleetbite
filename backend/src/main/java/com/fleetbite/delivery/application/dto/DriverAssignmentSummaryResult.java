package com.fleetbite.delivery.application.dto;

public record DriverAssignmentSummaryResult(
		long deliveriesCompletedToday,
		long assignmentsAccepted,
		long assignmentsRejected,
		int acceptanceRate) {
}
