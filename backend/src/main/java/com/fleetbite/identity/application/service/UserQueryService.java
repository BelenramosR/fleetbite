package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.UserQueryUseCase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class UserQueryService implements UserQueryUseCase {

	private final GetUserByIdService getUserByIdService;
	private final ListUsersService listUsersService;

	public UserQueryService(GetUserByIdService getUserByIdService, ListUsersService listUsersService) {
		this.getUserByIdService = Objects.requireNonNull(getUserByIdService);
		this.listUsersService = Objects.requireNonNull(listUsersService);
	}

	@Override
	public UserResult getById(UUID userId) {
		return getUserByIdService.execute(userId);
	}

	@Override
	public List<UserResult> list() {
		return listUsersService.execute();
	}
}
