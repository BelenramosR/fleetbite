package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.port.in.LoginUseCase;
import com.fleetbite.identity.infrastructure.inbound.rest.request.LoginRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.response.LoginResponse;
import com.fleetbite.shared.infrastructure.inbound.rest.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication")
public class AuthController {

	private final LoginUseCase loginUseCase;
	private final IdentityHttpMapper identityHttpMapper;

	public AuthController(LoginUseCase loginUseCase, IdentityHttpMapper identityHttpMapper) {
		this.loginUseCase = Objects.requireNonNull(loginUseCase);
		this.identityHttpMapper = Objects.requireNonNull(identityHttpMapper);
	}

	@PostMapping("/login")
	@SecurityRequirements
	@Operation(summary = "Authenticate user", description = "Issues a JWT access token. Does not require bearerAuth.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Login successful",
					content = @Content(schema = @Schema(implementation = LoginResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(responseCode = "401", description = "Invalid credentials (AUTHENTICATION_FAILED)",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
			@ApiResponse(responseCode = "403", description = "User inactive (USER_INACTIVE)",
					content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
	})
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		LoginResult result = loginUseCase.execute(identityHttpMapper.toCommand(request));
		return identityHttpMapper.toResponse(result);
	}
}
