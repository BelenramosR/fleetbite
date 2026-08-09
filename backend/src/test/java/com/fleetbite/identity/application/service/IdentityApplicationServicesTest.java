package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;
import com.fleetbite.identity.application.port.out.DriverProfileProvisionerPort;
import com.fleetbite.identity.application.port.out.PasswordEncoderPort;
import com.fleetbite.identity.application.port.out.RefreshTokenRepositoryPort;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.application.util.RefreshTokenHasher;
import com.fleetbite.identity.domain.exception.AuthenticationFailedException;
import com.fleetbite.identity.domain.exception.DuplicateUserEmailException;
import com.fleetbite.identity.domain.exception.UserInactiveException;
import com.fleetbite.identity.domain.model.RefreshToken;
import com.fleetbite.identity.domain.model.User;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.domain.model.UserRole;
import com.fleetbite.identity.domain.model.UserStatus;
import com.fleetbite.shared.domain.time.BusinessTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
	private DriverProfileProvisionerPort driverProfileProvisionerPort;
	@Mock
	private TokenProviderPort tokenProviderPort;
	@Mock
	private RefreshTokenRepositoryPort refreshTokenRepositoryPort;

	private CreateUserService createUserService;
	private LoginService loginService;
	private RefreshAccessTokenService refreshAccessTokenService;
	private LogoutService logoutService;
	private AuthTokenIssuer authTokenIssuer;

	@BeforeEach
	void setUp() {
		authTokenIssuer = new AuthTokenIssuer(
				tokenProviderPort, refreshTokenRepositoryPort, 604800L, FIXED_CLOCK);
		createUserService = new CreateUserService(
				userRepositoryPort, passwordEncoderPort, driverProfileProvisionerPort, FIXED_CLOCK);
		loginService = new LoginService(userRepositoryPort, passwordEncoderPort, authTokenIssuer);
		refreshAccessTokenService = new RefreshAccessTokenService(
				refreshTokenRepositoryPort, userRepositoryPort, authTokenIssuer, FIXED_CLOCK);
		logoutService = new LogoutService(refreshTokenRepositoryPort, FIXED_CLOCK);
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
		verify(driverProfileProvisionerPort, never()).provisionForDriverUser(any());
	}

	@Test
	void create_shouldProvisionDriverProfileWhenRoleIsDriver() {
		when(userRepositoryPort.existsByEmail("driver@fleetbite.local")).thenReturn(false);
		when(passwordEncoderPort.hash("Fleetbite1!")).thenReturn("$2b$hashed");
		when(userRepositoryPort.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		var result = createUserService.execute(
				new CreateUserCommand("driver@fleetbite.local", "Fleetbite1!", "Driver", UserRole.DRIVER));

		assertEquals(UserRole.DRIVER, result.role());
		verify(driverProfileProvisionerPort).provisionForDriverUser(UserId.of(result.id()));
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
		when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var result = loginService.execute(new LoginCommand("admin@fleetbite.local", "Fleetbite1!"));

		assertEquals("jwt-token", result.accessToken());
		assertEquals("Bearer", result.tokenType());
		assertEquals(3600L, result.expiresIn());
		assertNotNull(result.refreshToken());
		assertFalse(result.refreshToken().isBlank());

		ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
		verify(refreshTokenRepositoryPort).save(captor.capture());
		assertEquals(RefreshTokenHasher.sha256Hex(result.refreshToken()), captor.getValue().tokenHash());
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

	@Test
	void refresh_shouldRotateTokensWhenValid() {
		User user = activeUser();
		String rawRefresh = UUID.randomUUID().toString();
		RefreshToken existing = RefreshToken.issue(
				UUID.randomUUID(),
				user.id(),
				RefreshTokenHasher.sha256Hex(rawRefresh),
				NOW.minusHours(1),
				NOW.plusDays(6));

		when(refreshTokenRepositoryPort.findByHash(RefreshTokenHasher.sha256Hex(rawRefresh)))
				.thenReturn(Optional.of(existing));
		when(userRepositoryPort.findById(user.id())).thenReturn(Optional.of(user));
		when(tokenProviderPort.generate(eq(user.id()), eq(user.email()), eq(UserRole.ADMIN)))
				.thenReturn("new-jwt");
		when(tokenProviderPort.expiresInSeconds()).thenReturn(3600L);
		when(refreshTokenRepositoryPort.save(any(RefreshToken.class)))
				.thenAnswer(invocation -> invocation.getArgument(0));

		var result = refreshAccessTokenService.execute(new RefreshTokenCommand(rawRefresh));

		assertEquals("new-jwt", result.accessToken());
		assertNotNull(result.refreshToken());
		assertFalse(result.refreshToken().equals(rawRefresh));
		verify(refreshTokenRepositoryPort).revoke(eq(existing.id()), eq(NOW));
		verify(refreshTokenRepositoryPort).save(any(RefreshToken.class));
	}

	@Test
	void refresh_shouldFailWhenTokenRevoked() {
		User user = activeUser();
		String rawRefresh = UUID.randomUUID().toString();
		RefreshToken existing = RefreshToken.issue(
				UUID.randomUUID(),
				user.id(),
				RefreshTokenHasher.sha256Hex(rawRefresh),
				NOW.minusHours(1),
				NOW.plusDays(6));
		existing.revoke(NOW.minusMinutes(5));

		when(refreshTokenRepositoryPort.findByHash(RefreshTokenHasher.sha256Hex(rawRefresh)))
				.thenReturn(Optional.of(existing));

		assertThrows(
				AuthenticationFailedException.class,
				() -> refreshAccessTokenService.execute(new RefreshTokenCommand(rawRefresh)));
		verify(refreshTokenRepositoryPort, never()).revoke(any(), any());
		verify(refreshTokenRepositoryPort, never()).save(any());
	}

	@Test
	void refresh_shouldFailWhenTokenUnknown() {
		when(refreshTokenRepositoryPort.findByHash(anyString())).thenReturn(Optional.empty());

		assertThrows(
				AuthenticationFailedException.class,
				() -> refreshAccessTokenService.execute(new RefreshTokenCommand(UUID.randomUUID().toString())));
	}

	@Test
	void logout_shouldRevokeWhenPresent() {
		User user = activeUser();
		String rawRefresh = UUID.randomUUID().toString();
		RefreshToken existing = RefreshToken.issue(
				UUID.randomUUID(),
				user.id(),
				RefreshTokenHasher.sha256Hex(rawRefresh),
				NOW.minusHours(1),
				NOW.plusDays(6));

		when(refreshTokenRepositoryPort.findByHash(RefreshTokenHasher.sha256Hex(rawRefresh)))
				.thenReturn(Optional.of(existing));

		logoutService.execute(new RefreshTokenCommand(rawRefresh));

		verify(refreshTokenRepositoryPort).revoke(existing.id(), NOW);
	}

	@Test
	void logout_shouldBeIdempotentWhenMissing() {
		when(refreshTokenRepositoryPort.findByHash(anyString())).thenReturn(Optional.empty());

		logoutService.execute(new RefreshTokenCommand(UUID.randomUUID().toString()));

		verify(refreshTokenRepositoryPort, never()).revoke(any(), any());
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
