package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.port.out.PasswordEncoderPort;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.DuplicateUserEmailException;
import com.fleetbite.identity.domain.exception.UserInactiveException;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.identity.domain.model.UserStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentityApplicationServicesTest {

	private static final OffsetDateTime NOW =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);
	private static final Clock FIXED_CLOCK = Clock.fixed(NOW.toInstant(), BusinessTime.ZONE_OFFSET);

	@Mock
	private UserRepositoryPort userRepositoryPort;
	@Mock
	private PasswordEncoderPort passwordEncoderPort;
	@Mock
	private TokenProviderPort tokenProviderPort;

	private CreateUserService createUserService;
	private LoginService loginService;

	@BeforeEach
	void setUp() {
		createUserService = new CreateUserService(userRepositoryPort, passwordEncoderPort, FIXED_CLOCK);
		loginService = new LoginService(userRepositoryPort, passwordEncoderPort, tokenProviderPort);
	}

	@Test
	void create_shouldHashPasswordAndPersist() {
		when(userRepositoryPort.existsByEmail("admin@fleetbite.local")).thenReturn(false);
		when(passwordEncoderPort.hash("Fleetbite1!")).thenReturn("$2b$hashed");
		when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = createUserService.execute(
				new CreateUserCommand("admin@fleetbite.local", "Fleetbite1!", "Admin", UserRole.ADMIN));

		assertEquals(UserStatus.ACTIVE, result.status());
		assertEquals("admin@fleetbite.local", result.email());
		verify(passwordEncoderPort).hash("Fleetbite1!");
	}

	@Test
	void create_shouldRejectDuplicateEmail() {
		when(userRepositoryPort.existsByEmail("admin@fleetbite.local")).thenReturn(true);

		assertThrows(
				DuplicateUserEmailException.class,
				() -> createUserService.execute(
						new CreateUserCommand("admin@fleetbite.local", "Fleetbite1!", "Admin", UserRole.ADMIN)));
		verify(userRepositoryPort, never()).save(any());
	}

	@Test
	void login_shouldReturnTokenWhenValid() {
		User user = activeUser();
		when(userRepositoryPort.findByEmail("admin@fleetbite.local")).thenReturn(Optional.of(user));
		when(passwordEncoderPort.matches("Fleetbite1!", user.passwordHash())).thenReturn(true);
		when(tokenProviderPort.generate(eq(user.id()), eq(user.email()), eq(UserRole.ADMIN)))
				.thenReturn("jwt-token");
		when(tokenProviderPort.expiresInSeconds()).thenReturn(3600L);

		var result = loginService.execute(new LoginCommand("admin@fleetbite.local", "Fleetbite1!"));

		assertEquals("jwt-token", result.accessToken());
		assertEquals("Bearer", result.tokenType());
		assertEquals(3600L, result.expiresIn());
	}

	@Test
	void login_shouldFailOnBadPassword() {
		User user = activeUser();
		when(userRepositoryPort.findByEmail("admin@fleetbite.local")).thenReturn(Optional.of(user));
		when(passwordEncoderPort.matches(anyString(), anyString())).thenReturn(false);

		assertThrows(
				AuthenticationFailedException.class,
				() -> loginService.execute(new LoginCommand("admin@fleetbite.local", "wrong")));
	}

	@Test
	void login_shouldFailWhenInactive() {
		User user = activeUser();
		user.deactivate(NOW.plusMinutes(1));
		when(userRepositoryPort.findByEmail("admin@fleetbite.local")).thenReturn(Optional.of(user));
		when(passwordEncoderPort.matches("Fleetbite1!", user.passwordHash())).thenReturn(true);

		assertThrows(
				UserInactiveException.class,
				() -> loginService.execute(new LoginCommand("admin@fleetbite.local", "Fleetbite1!")));
	}

	private static User activeUser() {
		return User.create(
				UserId.generate(),
				"admin@fleetbite.local",
				"$2b$hash",
				"Admin",
				UserRole.ADMIN,
				NOW);
	}
}
