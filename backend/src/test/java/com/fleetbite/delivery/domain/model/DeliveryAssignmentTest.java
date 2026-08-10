package com.fleetbite.delivery.domain.model;

import java.util.UUID;

import com.fleetbite.delivery.domain.exception.InvalidAssignmentDataException;
import com.fleetbite.delivery.domain.exception.InvalidAssignmentTransitionException;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeliveryAssignmentTest {

	private static final OffsetDateTime ASSIGNED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final OffsetDateTime LATER =
			OffsetDateTime.of(2026, 8, 8, 22, 30, 0, 0, BusinessTime.ZONE_OFFSET);

	@Test
	void create_shouldStartPending() {
		DeliveryAssignment assignment = pendingAssignment();

		assertEquals(AssignmentStatus.PENDING, assignment.status());
		assertEquals(ASSIGNED_AT, assignment.assignedAt());
		assertEquals(ASSIGNED_AT, assignment.createdAt());
		assertNull(assignment.assignmentScore());
	}

	@Test
	void accept_shouldTransitionPendingToAccepted() {
		DeliveryAssignment assignment = pendingAssignment();

		assignment.accept(LATER);

		assertEquals(AssignmentStatus.ACCEPTED, assignment.status());
		assertEquals(LATER, assignment.acceptedAt());
	}

	@Test
	void reject_shouldRequireReasonAndTransition() {
		DeliveryAssignment assignment = pendingAssignment();

		assignment.reject("Vehicle problem", LATER);

		assertEquals(AssignmentStatus.REJECTED, assignment.status());
		assertEquals("Vehicle problem", assignment.rejectionReason());
		assertEquals(LATER, assignment.rejectedAt());
	}

	@Test
	void reject_shouldFailWithoutReason() {
		DeliveryAssignment assignment = pendingAssignment();

		assertThrows(InvalidAssignmentDataException.class, () -> assignment.reject("  ", LATER));
	}

	@Test
	void markPickedUp_shouldSetTimestampWithoutChangingStatus() {
		DeliveryAssignment assignment = pendingAssignment();
		assignment.accept(LATER);

		assignment.markPickedUp(LATER.plusMinutes(5));

		assertEquals(AssignmentStatus.ACCEPTED, assignment.status());
		assertNotNull(assignment.pickedUpAt());
	}

	@Test
	void markPickedUp_shouldRejectWhenNotAccepted() {
		DeliveryAssignment assignment = pendingAssignment();

		assertThrows(InvalidAssignmentTransitionException.class, () -> assignment.markPickedUp(LATER));
	}

	@Test
	void markPickedUp_shouldRejectWhenAlreadyPickedUp() {
		DeliveryAssignment assignment = pendingAssignment();
		assignment.accept(LATER);
		assignment.markPickedUp(LATER.plusMinutes(1));

		assertThrows(
				InvalidAssignmentTransitionException.class,
				() -> assignment.markPickedUp(LATER.plusMinutes(2)));
	}

	@Test
	void complete_shouldRequirePickupFirst() {
		DeliveryAssignment assignment = pendingAssignment();
		assignment.accept(LATER);

		assertThrows(InvalidAssignmentTransitionException.class, () -> assignment.complete(LATER.plusMinutes(1)));
	}

	@Test
	void complete_shouldTransitionAcceptedToCompleted() {
		DeliveryAssignment assignment = pendingAssignment();
		assignment.accept(LATER);
		assignment.markPickedUp(LATER.plusMinutes(1));

		assignment.complete(LATER.plusMinutes(2));

		assertEquals(AssignmentStatus.COMPLETED, assignment.status());
		assertEquals(LATER.plusMinutes(2), assignment.completedAt());
	}

	@Test
	void accept_shouldRejectFromRejected() {
		DeliveryAssignment assignment = pendingAssignment();
		assignment.reject("no", LATER);

		assertThrows(InvalidAssignmentTransitionException.class, () -> assignment.accept(LATER.plusMinutes(1)));
	}

	private static DeliveryAssignment pendingAssignment() {
		return DeliveryAssignment.create(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				ASSIGNED_AT);
	}
}
