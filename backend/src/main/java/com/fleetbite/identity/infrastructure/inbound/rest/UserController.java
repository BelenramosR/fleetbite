package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.CreateUserUseCase;
import com.fleetbite.identity.application.port.in.UpdateUserUseCase;
import com.fleetbite.identity.application.port.in.UserLifecycleUseCase;
import com.fleetbite.identity.application.port.in.UserQueryUseCase;
import com.fleetbite.identity.infrastructure.inbound.rest.request.CreateUserRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.request.UpdateUserRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.response.UserResponse;
import com.fleetbite.shared.infrastructure.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

	private final CreateUserUseCase createUserUseCase;
	private final UserQueryUseCase userQueryUseCase;
	private final UpdateUserUseCase updateUserUseCase;
	private final UserLifecycleUseCase userLifecycleUseCase;
	private final IdentityHttpMapper identityHttpMapper;

	public UserController(
			CreateUserUseCase createUserUseCase,
			UserQueryUseCase userQueryUseCase,
			UpdateUserUseCase updateUserUseCase,
			UserLifecycleUseCase userLifecycleUseCase,
			IdentityHttpMapper identityHttpMapper) {
		this.createUserUseCase = Objects.requireNonNull(createUserUseCase);
		this.userQueryUseCase = Objects.requireNonNull(userQueryUseCase);
		this.updateUserUseCase = Objects.requireNonNull(updateUserUseCase);
		this.userLifecycleUseCase = Objects.requireNonNull(userLifecycleUseCase);
		this.identityHttpMapper = Objects.requireNonNull(identityHttpMapper);
	}

	@GetMapping
	@Operation(summary = "List users", description = "ADMIN only")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Users returned"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public List<UserResponse> listUsers() {
		return userQueryUseCase.list().stream()
				.map(identityHttpMapper::toResponse)
				.toList();
	}

	@PostMapping
	@Operation(summary = "Create user",
			description = "ADMIN only. Password is hashed; never returned. "
					+ "When role=DRIVER, a Driver profile is provisioned automatically (phone/vehicle/location set later).")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "User created"),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Duplicate email",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
		UserResult result = createUserUseCase.execute(identityHttpMapper.toCommand(request));
		URI location = ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(result.id())
				.toUri();
		return ResponseEntity.created(location).body(identityHttpMapper.toResponse(result));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by id", description = "ADMIN only")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User found"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public UserResponse getUserById(@PathVariable UUID id) {
		return identityHttpMapper.toResponse(userQueryUseCase.getById(id));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update user profile", description = "ADMIN only. Does not change password.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User updated"),
			@ApiResponse(responseCode = "400", description = "Validation error",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public UserResponse updateUser(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request) {
		UserResult result = updateUserUseCase.execute(id, identityHttpMapper.toCommand(request));
		return identityHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/activate")
	@Operation(summary = "Activate user", description = "ADMIN only")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User activated"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public UserResponse activate(@PathVariable UUID id) {
		return identityHttpMapper.toResponse(userLifecycleUseCase.activate(id));
	}

	@PostMapping("/{id}/deactivate")
	@Operation(summary = "Deactivate user", description = "ADMIN only")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "User deactivated"),
			@ApiResponse(responseCode = "401", description = "Unauthenticated",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "403", description = "Forbidden",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not found",
					content = @Content(schema = @Schema(implementation = com.fleetbite.shared.infrastructure.inbound.rest.ApiResponse.class)))
	})
	public UserResponse deactivate(@PathVariable UUID id) {
		return identityHttpMapper.toResponse(userLifecycleUseCase.deactivate(id));
	}
}
