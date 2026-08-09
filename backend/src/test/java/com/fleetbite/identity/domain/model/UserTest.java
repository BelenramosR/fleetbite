package com.fleetbite.identity.domain.model;

import com.fleetbite.identity.domain.exception.InvalidUserDataException;
import com.fleetbite.identity.domain.exception.UserInactiveException;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Test
	void create_shouldStartActiveAndNormalizeEmail() {
		User user = User.create(
				UserId.generate(),
				" Admin@FleetBite.Local ",
				"$2b$hash",
				"Admin",
				UserRole.ADMIN,
				CREATED);

		assertEquals("admin@fleetbite.local", user.email());
		assertEquals(UserStatus.ACTIVE, user.status());
		assertEquals(CREATED, user.updatedAt());
	}

	@Test
	void deactivate_and_ensureActive_shouldFailWhenInactive() {
		User user = User.create(
				UserId.generate(),
				"admin@fleetbite.local",
				"$2b$hash",
				"Admin",
				UserRole.ADMIN,
				CREATED);
		user.deactivate(CREATED.plusMinutes(1));

		assertEquals(UserStatus.INACTIVE, user.status());
		assertThrows(UserInactiveException.class, user::ensureActive);
	}

	@Test
	void updateProfile_shouldChangeNameAndRole() {
		User user = User.create(
				UserId.generate(),
				"admin@fleetbite.local",
				"$2b$hash",
				"Admin",
				UserRole.ADMIN,
				CREATED);

		user.updateProfile("New Name", UserRole.DISPATCHER, CREATED.plusMinutes(2));

		assertEquals("New Name", user.fullName());
		assertEquals(UserRole.DISPATCHER, user.role());
	}

	@Test
	void create_shouldRejectInvalidEmail() {
		assertThrows(
				InvalidUserDataException.class,
				() -> User.create(
						UserId.generate(),
						"not-an-email",
						"$2b$hash",
						"Admin",
						UserRole.ADMIN,
						CREATED));
	}
}
