package com.fleetbite.driver.infrastructure.config;

import com.fleetbite.driver.application.port.in.AssignVehicleToDriverUseCase;
import com.fleetbite.driver.application.port.in.DeleteDriverUseCase;
import com.fleetbite.driver.application.port.in.GetDriverByIdUseCase;
import com.fleetbite.driver.application.port.in.ListDriversUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOfflineUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOnlineUseCase;
import com.fleetbite.driver.application.port.in.UnassignVehicleFromDriverUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverLocationUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.application.service.AssignVehicleToDriverService;
import com.fleetbite.driver.application.service.DeleteDriverService;
import com.fleetbite.driver.application.service.ProvisionDriverProfileService;
import com.fleetbite.driver.application.service.GetDriverByIdService;
import com.fleetbite.driver.application.service.ListDriversService;
import com.fleetbite.driver.application.service.SetDriverOfflineService;
import com.fleetbite.driver.application.service.SetDriverOnlineService;
import com.fleetbite.driver.application.service.UnassignVehicleFromDriverService;
import com.fleetbite.driver.application.service.UpdateDriverLocationService;
import com.fleetbite.driver.application.service.UpdateDriverService;
import com.fleetbite.driver.infrastructure.transaction.TransactionalAssignVehicleToDriverUseCase;
import com.fleetbite.driver.infrastructure.transaction.TransactionalUnassignVehicleFromDriverUseCase;
import com.fleetbite.identity.application.port.out.DriverProfileProvisionerPort;
import com.fleetbite.identity.application.port.out.UserRepositoryPort;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DriverApplicationConfig {

	@Bean
	DriverProfileProvisionerPort driverProfileProvisionerPort(
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		return new ProvisionDriverProfileService(driverRepositoryPort, clock);
	}

	@Bean
	GetDriverByIdUseCase getDriverByIdUseCase(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort) {
		return new GetDriverByIdService(driverRepositoryPort, userRepositoryPort, vehicleRepositoryPort);
	}

	@Bean
	ListDriversUseCase listDriversUseCase(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort) {
		return new ListDriversService(driverRepositoryPort, userRepositoryPort, vehicleRepositoryPort);
	}

	@Bean
	UpdateDriverUseCase updateDriverUseCase(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			Clock clock) {
		return new UpdateDriverService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort,
				clock);
	}

	@Bean
	DeleteDriverUseCase deleteDriverUseCase(DriverRepositoryPort driverRepositoryPort) {
		return new DeleteDriverService(driverRepositoryPort);
	}

	@Bean
	UpdateDriverLocationUseCase updateDriverLocationUseCase(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			Clock clock) {
		return new UpdateDriverLocationService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort,
				clock);
	}

	@Bean
	SetDriverOnlineUseCase setDriverOnlineUseCase(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			Clock clock) {
		return new SetDriverOnlineService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort,
				clock);
	}

	@Bean
	SetDriverOfflineUseCase setDriverOfflineUseCase(
			DriverRepositoryPort driverRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			Clock clock) {
		return new SetDriverOfflineService(
				driverRepositoryPort,
				userRepositoryPort,
				vehicleRepositoryPort,
				clock);
	}

	@Bean
	AssignVehicleToDriverUseCase assignVehicleToDriverUseCase(
			DriverRepositoryPort driverRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			Clock clock) {
		return new TransactionalAssignVehicleToDriverUseCase(
				new AssignVehicleToDriverService(
						driverRepositoryPort,
						vehicleRepositoryPort,
						userRepositoryPort,
						clock));
	}

	@Bean
	UnassignVehicleFromDriverUseCase unassignVehicleFromDriverUseCase(
			DriverRepositoryPort driverRepositoryPort,
			VehicleRepositoryPort vehicleRepositoryPort,
			UserRepositoryPort userRepositoryPort,
			Clock clock) {
		return new TransactionalUnassignVehicleFromDriverUseCase(
				new UnassignVehicleFromDriverService(
						driverRepositoryPort,
						vehicleRepositoryPort,
						userRepositoryPort,
						clock));
	}
}
