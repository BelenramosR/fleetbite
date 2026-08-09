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
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IdentityHttpMapper {

	LoginCommand toCommand(LoginRequest request);

	RefreshTokenCommand toCommand(RefreshTokenRequest request);

	CreateUserCommand toCommand(CreateUserRequest request);

	UpdateUserCommand toCommand(UpdateUserRequest request);

	LoginResponse toResponse(LoginResult result);

	@Mapping(target = "id", expression = "java(result.id())")
	@Mapping(target = "email", expression = "java(result.email())")
	@Mapping(target = "fullName", expression = "java(result.fullName())")
	@Mapping(target = "role", expression = "java(result.role().name())")
	@Mapping(target = "status", expression = "java(result.status().name())")
	@Mapping(target = "createdAt", expression = "java(result.createdAt())")
	@Mapping(target = "updatedAt", expression = "java(result.updatedAt())")
	UserResponse toResponse(UserResult result);
}
