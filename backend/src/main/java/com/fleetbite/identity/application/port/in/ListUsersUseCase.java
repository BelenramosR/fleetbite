package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.UserResult;

import java.util.List;

public interface ListUsersUseCase {

	List<UserResult> execute();
}
