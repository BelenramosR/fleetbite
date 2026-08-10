package com.fleetbite.identity.application.port.in;

import com.fleetbite.identity.application.dto.UserResult;

import java.util.List;
import java.util.UUID;

public interface UserQueryUseCase {

	UserResult getById(UUID userId);

	List<UserResult> list();
}
