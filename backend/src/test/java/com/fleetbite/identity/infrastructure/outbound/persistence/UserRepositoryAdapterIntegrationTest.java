package com.fleetbite.identity.infrastructure.outbound.persistence;

import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.identity.domain.model.UserStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = {
		"spring.jpa.hibernate.ddl-auto=validate",
		"spring.flyway.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({UserRepositoryAdapter.class, UserPersistenceMapper.class})
@Testcontainers
class UserRepositoryAdapterIntegrationTest {

	private static final OffsetDateTime CREATED_AT =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

	@Autowired
	private UserRepositoryPort userRepositoryPort;

	@Test
	void seed_shouldContainDemoUsers() {
		Optional<User> admin = userRepositoryPort.findByEmail("admin@fleetbite.local");
		assertTrue(admin.isPresent());
		assertEquals(UserRole.ADMIN, admin.get().role());
		assertEquals(UserStatus.ACTIVE, admin.get().status());

		List<User> all = userRepositoryPort.findAll();
		assertEquals(4, all.size());
	}

	@Test
	void saveAndUpdate_shouldPersistUser() {
		UserId id = UserId.of(UUID.fromString("55555555-5555-5555-5555-555555555555"));
		User saved = userRepositoryPort.save(User.create(
				id,
				"new.user@fleetbite.local",
				"$2a$10$hashhashhashhashhashhashhashhashhashhashhashhashhashhu",
				"New User",
				UserRole.DISPATCHER,
				CREATED_AT));

		assertEquals("new.user@fleetbite.local", saved.email());

		saved.updateProfile("Renamed User", UserRole.ADMIN, CREATED_AT.plusMinutes(1));
		User updated = userRepositoryPort.update(saved);

		assertEquals("Renamed User", updated.fullName());
		assertEquals(UserRole.ADMIN, updated.role());
		assertEquals(CREATED_AT.plusMinutes(1), updated.updatedAt());
	}
}
