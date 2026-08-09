package com.fleetbite.identity.infrastructure.inbound.rest;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.LoginCommand;
import com.fleetbite.identity.application.dto.LoginResult;
import com.fleetbite.identity.application.dto.RefreshTokenCommand;
import com.fleetbite.identity.application.dto.UpdateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.infrastructure.inbound.rest.request.CreateUserRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.request.LoginRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.request.RefreshTokenRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.request.UpdateUserRequest;
import com.fleetbite.identity.infrastructure.inbound.rest.response.LoginResponse;
import com.fleetbite.identity.infrastructure.inbound.rest.response.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class IdentityHttpMapper {

	public LoginCommand toCommand(LoginRequest request) {
		return new LoginCommand(request.email(), request.password());
	}

	public RefreshTokenCommand toCommand(RefreshTokenRequest request) {
		return new RefreshTokenCommand(request.refreshToken());
	}

	public CreateUserCommand toCommand(CreateUserRequest request) {
		return new CreateUserCommand(
				request.email(),
				request.password(),
				request.fullName(),
				request.role());
	}

	public UpdateUserCommand toCommand(UpdateUserRequest request) {
		return new UpdateUserCommand(request.fullName(), request.role());
	}

	public LoginResponse toResponse(LoginResult result) {
		return new LoginResponse(
				result.accessToken(),
				result.tokenType(),
				result.expiresIn(),
				result.refreshToken());
	}

	public UserResponse toResponse(UserResult result) {
		return new UserResponse(
				result.id(),
				result.email(),
				result.fullName(),
				result.role().name(),
				result.status().name(),
				result.createdAt(),
				result.updatedAt());
	}
}
