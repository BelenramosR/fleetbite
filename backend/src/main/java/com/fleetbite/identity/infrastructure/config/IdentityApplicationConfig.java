package com.fleetbite.identity.infrastructure.config;

import com.fleetbite.identity.application.port.in.*;
import com.fleetbite.identity.application.port.out.*;
import com.fleetbite.identity.application.service.*;
import com.fleetbite.identity.infrastructure.jwt.JwtProperties;
import com.fleetbite.shared.infrastructure.transaction.TransactionProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

@Configuration
public class IdentityApplicationConfig {
	@Bean
	AuthTokenIssuer authTokenIssuer(TokenProviderPort tokens, RefreshTokenRepositoryPort refreshTokens,
			JwtProperties properties, Clock clock) {
		return new AuthTokenIssuer(tokens, refreshTokens, properties.getRefreshExpirationSeconds(), clock);
	}

	@Bean
	AuthenticationUseCase authenticationUseCase(UserRepositoryPort users, PasswordEncoderPort passwords,
			RefreshTokenRepositoryPort refreshTokens, AuthTokenIssuer issuer, Clock clock,
			PlatformTransactionManager transactions) {
		AuthenticationService service = new AuthenticationService(
				new LoginService(users, passwords, issuer),
				new RefreshAccessTokenService(refreshTokens, users, issuer, clock),
				new LogoutService(refreshTokens, clock));
		return TransactionProxyFactory.readWrite(AuthenticationUseCase.class, service, transactions);
	}

	@Bean
	CreateUserUseCase createUserUseCase(UserRepositoryPort users, PasswordEncoderPort passwords,
			DriverProfileProvisionerPort drivers, Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(CreateUserUseCase.class,
				new CreateUserService(users, passwords, drivers, clock), transactions);
	}

	@Bean
	UserQueryUseCase userQueryUseCase(UserRepositoryPort users, PlatformTransactionManager transactions) {
		UserQueryService service = new UserQueryService(new GetUserByIdService(users), new ListUsersService(users));
		return TransactionProxyFactory.readOnly(UserQueryUseCase.class, service, transactions);
	}

	@Bean
	UpdateUserUseCase updateUserUseCase(UserRepositoryPort users, Clock clock,
			PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(UpdateUserUseCase.class,
				new UpdateUserService(users, clock), transactions);
	}

	@Bean
	UserLifecycleUseCase userLifecycleUseCase(UserRepositoryPort users, Clock clock,
			PlatformTransactionManager transactions) {
		UserLifecycleService service = new UserLifecycleService(
				new ActivateUserService(users, clock), new DeactivateUserService(users, clock));
		return TransactionProxyFactory.readWrite(UserLifecycleUseCase.class, service, transactions);
	}
}
