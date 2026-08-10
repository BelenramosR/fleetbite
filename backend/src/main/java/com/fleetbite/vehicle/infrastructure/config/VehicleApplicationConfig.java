package com.fleetbite.vehicle.infrastructure.config;

import com.fleetbite.shared.infrastructure.transaction.TransactionProxyFactory;
import com.fleetbite.vehicle.application.port.in.*;
import com.fleetbite.vehicle.application.port.out.VehicleAssignmentLookupPort;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.application.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;

@Configuration
public class VehicleApplicationConfig {

	@Bean
	CreateVehicleUseCase createVehicleUseCase(
			VehicleRepositoryPort vehicles, Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				CreateVehicleUseCase.class, new CreateVehicleService(vehicles, clock), transactions);
	}

	@Bean
	VehicleQueryUseCase vehicleQueryUseCase(
			VehicleRepositoryPort vehicles, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readOnly(
				VehicleQueryUseCase.class,
				new VehicleQueryService(new GetVehicleByIdService(vehicles), new ListVehiclesService(vehicles)),
				transactions);
	}

	@Bean
	UpdateVehicleUseCase updateVehicleUseCase(
			VehicleRepositoryPort vehicles, Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				UpdateVehicleUseCase.class, new UpdateVehicleService(vehicles, clock), transactions);
	}

	@Bean
	DeleteVehicleUseCase deleteVehicleUseCase(
			VehicleRepositoryPort vehicles,
			VehicleAssignmentLookupPort assignments,
			PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				DeleteVehicleUseCase.class,
				new DeleteVehicleService(vehicles, assignments), transactions);
	}

	@Bean
	VehicleLifecycleUseCase vehicleLifecycleUseCase(
			VehicleRepositoryPort vehicles, Clock clock, PlatformTransactionManager transactions) {
		return TransactionProxyFactory.readWrite(
				VehicleLifecycleUseCase.class,
				new VehicleLifecycleService(
						new SendVehicleToMaintenanceService(vehicles, clock),
						new ActivateVehicleService(vehicles, clock),
						new DeactivateVehicleService(vehicles, clock)), transactions);
	}
}
