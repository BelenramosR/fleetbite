package com.fleetbite.identity.infrastructure.transaction;

import com.fleetbite.identity.application.dto.CreateUserCommand;
import com.fleetbite.identity.application.dto.UserResult;
import com.fleetbite.identity.application.port.in.CreateUserUseCase;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

public class TransactionalCreateUserUseCase implements CreateUserUseCase {

	private final CreateUserUseCase delegate;

	public TransactionalCreateUserUseCase(CreateUserUseCase delegate) {
		this.delegate = Objects.requireNonNull(delegate);
	}

	@Override
	@Transactional
	public UserResult execute(CreateUserCommand command) {
		return delegate.execute(command);
	}
}
