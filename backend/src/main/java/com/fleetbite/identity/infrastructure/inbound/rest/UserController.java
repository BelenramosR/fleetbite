package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.ActivateUserUseCase;
import com.fleetbite.identity.application.port.in.CreateUserUseCase;
import com.fleetbite.identity.application.port.in.DeactivateUserUseCase;
import com.fleetbite.identity.application.port.in.GetUserByIdUseCase;
import com.fleetbite.identity.application.port.in.ListUsersUseCase;
import com.fleetbite.identity.application.port.in.UpdateUserUseCase;
import com.fleetbite.identity.domain.model.UserId;
import com.fleetbite.identity.infrastructure.inbound.rest.request.CreateUserRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.request.UpdateUserRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.response.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
public class UserController {

	private final CreateUserUseCase createUserUseCase;
	private final GetUserByIdUseCase getUserByIdUseCase;
	private final ListUsersUseCase listUsersUseCase;
	private final UpdateUserUseCase updateUserUseCase;
	private final ActivateUserUseCase activateUserUseCase;
	private final DeactivateUserUseCase deactivateUserUseCase;
	private final IdentityHttpMapper identityHttpMapper;

	public UserController(
			CreateUserUseCase createUserUseCase,
			GetUserByIdUseCase getUserByIdUseCase,
			ListUsersUseCase listUsersUseCase,
			UpdateUserUseCase updateUserUseCase,
			ActivateUserUseCase activateUserUseCase,
			DeactivateUserUseCase deactivateUserUseCase,
			IdentityHttpMapper identityHttpMapper) {
		this.createUserUseCase = Objects.requireNonNull(createUserUseCase);
		this.getUserByIdUseCase = Objects.requireNonNull(getUserByIdUseCase);
		this.listUsersUseCase = Objects.requireNonNull(listUsersUseCase);
		this.updateUserUseCase = Objects.requireNonNull(updateUserUseCase);
		this.activateUserUseCase = Objects.requireNonNull(activateUserUseCase);
		this.deactivateUserUseCase = Objects.requireNonNull(deactivateUserUseCase);
		this.identityHttpMapper = Objects.requireNonNull(identityHttpMapper);
	}

	@GetMapping
	public List<UserResponse> listUsers() {
		return listUsersUseCase.execute().stream()
				.map(identityHttpMapper::toResponse)
				.toList();
	}

	@PostMapping
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
	public UserResponse getUserById(@PathVariable UUID id) {
		return identityHttpMapper.toResponse(getUserByIdUseCase.execute(UserId.of(id)));
	}

	@PutMapping("/{id}")
	public UserResponse updateUser(
			@PathVariable UUID id,
			@Valid @RequestBody UpdateUserRequest request) {
		UserResult result = updateUserUseCase.execute(UserId.of(id), identityHttpMapper.toCommand(request));
		return identityHttpMapper.toResponse(result);
	}

	@PostMapping("/{id}/activate")
	public UserResponse activate(@PathVariable UUID id) {
		return identityHttpMapper.toResponse(activateUserUseCase.execute(UserId.of(id)));
	}

	@PostMapping("/{id}/deactivate")
	public UserResponse deactivate(@PathVariable UUID id) {
		return identityHttpMapper.toResponse(deactivateUserUseCase.execute(UserId.of(id)));
	}
}
