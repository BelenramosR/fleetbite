package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.in.AuthenticationUseCase;
import com.fleetbite.identity.infrastructure.inbound.rest.request.LoginRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.request.RefreshTokenRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.response.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

	private final AuthenticationUseCase authenticationUseCase;
	private final IdentityHttpMapper identityHttpMapper;

	public AuthController(
			AuthenticationUseCase authenticationUseCase,
			IdentityHttpMapper identityHttpMapper) {
		this.authenticationUseCase = Objects.requireNonNull(authenticationUseCase);
		this.identityHttpMapper = Objects.requireNonNull(identityHttpMapper);
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Authenticate user",
			description = "Issues a JWT access token and an opaque refresh token. Does not require bearerAuth.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login successful",
					content = @Content(schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Invalid credentials (AUTHENTICATION_FAILED)",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "User inactive (USER_INACTIVE)",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = authenticationUseCase.login(identityHttpMapper.toCommand(request));
		return identityHttpMapper.toResponse(result);
	}

	@PostMapping("/refresh")
	@SecurityRequirements
	@Operation(summary = "Refresh access token",
			description = "Rotates the refresh token and issues a new access token. Does not require bearerAuth.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Tokens refreshed",
					content = @Content(schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Invalid or revoked refresh token (AUTHENTICATION_FAILED)",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "User inactive (USER_INACTIVE)",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		LoginResult result = authenticationUseCase.refresh(identityHttpMapper.toCommand(request));
		return identityHttpMapper.toResponse(result);
	}

	@PostMapping("/logout")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityRequirements
	@Operation(summary = "Logout",
			description = "Revokes the given refresh token. Idempotent: unknown or already revoked tokens still return 204.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Refresh token revoked (or already invalid)"),
			@ApiResponse(responseCode = "400", description = "Invalid request",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public void logout(@Valid @RequestBody RefreshTokenRequest request) {
		authenticationUseCase.logout(identityHttpMapper.toCommand(request));
	}
}
