package com.fleetbite.infrastructure.inbound.rest.driver;

public record DriverAssignmentSummaryResponse(
		long deliveriesCompletedToday,
		long assignmentsAccepted,
		long assignmentsRejected,
		int acceptanceRate) {
}
