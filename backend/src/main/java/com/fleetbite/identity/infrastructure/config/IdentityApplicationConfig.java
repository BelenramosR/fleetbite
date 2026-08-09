package com.fleetbite.identity.infrastructure.config;

import com.fleetbite.identity.application.port.in.ActivateUserUseCase;
import com.fleetbite.identity.application.port.in.CreateUserUseCase;
import com.fleetbite.identity.application.port.in.DeactivateUserUseCase;
import com.fleetbite.identity.application.port.in.GetUserByIdUseCase;
import com.fleetbite.identity.application.port.in.ListUsersUseCase;
import com.fleetbite.identity.application.port.in.LoginUseCase;
import com.fleetbite.identity.application.port.in.UpdateUserUseCase;
import com.fleetbite.identity.application.port.out.PasswordEncoderPort;
import com.fleetbite.identity.application.port.out.TokenProviderPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.identity.application.service.ActivateUserService;
import com.fleetbite.identity.application.service.CreateUserService;
import com.fleetbite.identity.application.service.DeactivateUserService;
import com.fleetbite.identity.application.service.GetUserByIdService;
import com.fleetbite.identity.application.service.ListUsersService;
import com.fleetbite.identity.application.service.LoginService;
import com.fleetbite.identity.application.service.UpdateUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class IdentityApplicationConfig {

	@Bean
	LoginUseCase loginUseCase(
			UserRepositoryPort userRepositoryPort,
			PasswordEncoderPort passwordEncoderPort,
			TokenProviderPort tokenProviderPort) {
		return new LoginService(userRepositoryPort, passwordEncoderPort, tokenProviderPort);
	}

	@Bean
	CreateUserUseCase createUserUseCase(
			UserRepositoryPort userRepositoryPort,
			PasswordEncoderPort passwordEncoderPort,
			Clock clock) {
		return new CreateUserService(userRepositoryPort, passwordEncoderPort, clock);
	}

	@Bean
	GetUserByIdUseCase getUserByIdUseCase(UserRepositoryPort userRepositoryPort) {
		return new GetUserByIdService(userRepositoryPort);
	}

	@Bean
	ListUsersUseCase listUsersUseCase(UserRepositoryPort userRepositoryPort) {
		return new ListUsersService(userRepositoryPort);
	}

	@Bean
	UpdateUserUseCase updateUserUseCase(UserRepositoryPort userRepositoryPort, Clock clock) {
		return new UpdateUserService(userRepositoryPort, clock);
	}

	@Bean
	ActivateUserUseCase activateUserUseCase(UserRepositoryPort userRepositoryPort, Clock clock) {
		return new ActivateUserService(userRepositoryPort, clock);
	}

	@Bean
	DeactivateUserUseCase deactivateUserUseCase(UserRepositoryPort userRepositoryPort, Clock clock) {
		return new DeactivateUserService(userRepositoryPort, clock);
	}
}
