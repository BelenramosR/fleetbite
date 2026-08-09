package com.fleetbite.vehicle.infrastructure.config;

import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.vehicle.application.port.in.ActivateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.CreateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.DeactivateVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.DeleteVehicleUseCase;
import com.fleetbite.vehicle.application.port.in.GetVehicleByIdUseCase;
import com.fleetbite.vehicle.application.port.in.ListVehiclesUseCase;
import com.fleetbite.vehicle.application.port.in.SendVehicleToMaintenanceUseCase;
import com.fleetbite.vehicle.application.port.in.UpdateVehicleUseCase;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.application.service.ActivateVehicleService;
import com.fleetbite.vehicle.application.service.CreateVehicleService;
import com.fleetbite.vehicle.application.service.DeactivateVehicleService;
import com.fleetbite.vehicle.application.service.DeleteVehicleService;
import com.fleetbite.vehicle.application.service.GetVehicleByIdService;
import com.fleetbite.vehicle.application.service.ListVehiclesService;
import com.fleetbite.vehicle.application.service.SendVehicleToMaintenanceService;
import com.fleetbite.vehicle.application.service.UpdateVehicleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class VehicleApplicationConfig {

	@Bean
	CreateVehicleUseCase createVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		return new CreateVehicleService(vehicleRepositoryPort, clock);
	}

	@Bean
	GetVehicleByIdUseCase getVehicleByIdUseCase(VehicleRepositoryPort vehicleRepositoryPort) {
		return new GetVehicleByIdService(vehicleRepositoryPort);
	}

	@Bean
	ListVehiclesUseCase listVehiclesUseCase(VehicleRepositoryPort vehicleRepositoryPort) {
		return new ListVehiclesService(vehicleRepositoryPort);
	}

	@Bean
	UpdateVehicleUseCase updateVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		return new UpdateVehicleService(vehicleRepositoryPort, clock);
	}

	@Bean
	DeleteVehicleUseCase deleteVehicleUseCase(
			VehicleRepositoryPort vehicleRepositoryPort,
			DriverRepositoryPort driverRepositoryPort) {
		return new DeleteVehicleService(vehicleRepositoryPort, driverRepositoryPort);
	}

	@Bean
	SendVehicleToMaintenanceUseCase sendVehicleToMaintenanceUseCase(
			VehicleRepositoryPort vehicleRepositoryPort,
			Clock clock) {
		return new SendVehicleToMaintenanceService(vehicleRepositoryPort, clock);
	}

	@Bean
	ActivateVehicleUseCase activateVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		return new ActivateVehicleService(vehicleRepositoryPort, clock);
	}

	@Bean
	DeactivateVehicleUseCase deactivateVehicleUseCase(VehicleRepositoryPort vehicleRepositoryPort, Clock clock) {
		return new DeactivateVehicleService(vehicleRepositoryPort, clock);
	}
}
