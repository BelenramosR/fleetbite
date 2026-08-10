package com.fleetbite.driver.infrastructure.config;

import com.fleetbite.driver.application.port.in.*;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.application.service.*;
import com.fleetbite.identity.application.port.out.DriverProfileProvisionerPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.shared.infrastructure.transaction.TransactionProxyFactory;
import com.fleetbite.shared.domain.model.Location;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

@Configuration
public class DriverApplicationConfig {

	@Bean
	DriverProfileProvisionerPort driverProfileProvisionerPort(
			DriverRepositoryPort drivers, Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				DriverProfileProvisionerPort.class,
				new ProvisionDriverProfileService(drivers, clock), transactions);
	}

	@Bean
	DriverQueryUseCase driverQueryUseCase(
			DriverRepositoryPort drivers, UserRepositoryPort users, VehicleRepositoryPort vehicles,
			PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readOnly(
				DriverQueryUseCase.class,
				new DriverQueryService(
						new GetDriverByIdService(drivers, users, vehicles),
						new ListDriversService(drivers, users, vehicles)), transactions);
	}

	@Bean
	UpdateDriverUseCase updateDriverUseCase(
			DriverRepositoryPort drivers, UserRepositoryPort users, VehicleRepositoryPort vehicles,
			Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				UpdateDriverUseCase.class,
				new UpdateDriverService(drivers, users, vehicles, clock), transactions);
	}

	@Bean
	DeleteDriverUseCase deleteDriverUseCase(
			DriverRepositoryPort drivers, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				DeleteDriverUseCase.class, new DeleteDriverService(drivers), transactions);
	}

	@Bean
	UpdateDriverLocationUseCase updateDriverLocationUseCase(
			DriverRepositoryPort drivers, UserRepositoryPort users, VehicleRepositoryPort vehicles,
			Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				UpdateDriverLocationUseCase.class,
				new UpdateDriverLocationService(drivers, users, vehicles, clock), transactions);
	}

	@Bean
	DriverAvailabilityUseCase driverAvailabilityUseCase(
			DriverRepositoryPort drivers, UserRepositoryPort users, VehicleRepositoryPort vehicles,
			Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				DriverAvailabilityUseCase.class,
				new DriverAvailabilityService(
						new SetDriverOnlineService(drivers, users, vehicles, clock),
						new SetDriverOfflineService(drivers, users, vehicles, clock)), transactions);
	}

	@Bean
	DriverVehicleUseCase driverVehicleUseCase(
			DriverRepositoryPort drivers, VehicleRepositoryPort vehicles, UserRepositoryPort users,
			@Qualifier("restaurantLocation") Location restaurantLocation,
			Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				DriverVehicleUseCase.class,
				new DriverVehicleService(
						new AssignVehicleToDriverService(
								drivers, vehicles, users, restaurantLocation, clock),
						new UnassignVehicleFromDriverService(drivers, vehicles, users, clock)), transactions);
	}

	@Bean
	DriverSelfUseCase driverSelfUseCase(
			DriverRepositoryPort drivers,
			DriverQueryUseCase queries,
			UpdateDriverLocationUseCase location,
			DriverAvailabilityUseCase availability,
			PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				DriverSelfUseCase.class,
				new DriverSelfService(drivers, queries, location, availability), transactions);
	}
}
