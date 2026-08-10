package com.fleetbite.identity.application.service;

import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;

import java.util.List;
import java.util.Objects;

public final class ListUsersService {

	private final UserRepositoryPort userRepositoryPort;

	public ListUsersService(UserRepositoryPort userRepositoryPort) {
		this.userRepositoryPort = Objects.requireNonNull(userRepositoryPort);
	}

	public List<UserResult> execute() {
		return userRepositoryPort.findAll().stream()
				.map(UserResult::from)
				.toList();
	}
}
