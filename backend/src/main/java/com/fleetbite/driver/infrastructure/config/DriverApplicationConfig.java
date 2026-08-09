package com.fleetbite.driver.infrastructure.config;

import com.fleetbite.driver.application.port.in.CreateDriverUseCase;
import com.fleetbite.driver.application.port.in.DeleteDriverUseCase;
import com.fleetbite.driver.application.port.in.GetDriverByIdUseCase;
import com.fleetbite.driver.application.port.in.ListDriversUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOfflineUseCase;
import com.fleetbite.driver.application.port.in.SetDriverOnlineUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverLocationUseCase;
import com.fleetbite.driver.application.port.in.UpdateDriverUseCase;
import com.fleetbite.driver.application.port.out.DriverRepositoryPort;
import com.fleetbite.driver.application.service.CreateDriverService;
import com.fleetbite.driver.application.service.DeleteDriverService;
import com.fleetbite.driver.application.service.GetDriverByIdService;
import com.fleetbite.driver.application.service.ListDriversService;
import com.fleetbite.driver.application.service.SetDriverOfflineService;
import com.fleetbite.driver.application.service.SetDriverOnlineService;
import com.fleetbite.driver.application.service.UpdateDriverLocationService;
import com.fleetbite.driver.application.service.UpdateDriverService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class DriverApplicationConfig {

	@Bean
	CreateDriverUseCase createDriverUseCase(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		return new CreateDriverService(driverRepositoryPort, clock);
	}

	@Bean
	GetDriverByIdUseCase getDriverByIdUseCase(DriverRepositoryPort driverRepositoryPort) {
		return new GetDriverByIdService(driverRepositoryPort);
	}

	@Bean
	ListDriversUseCase listDriversUseCase(DriverRepositoryPort driverRepositoryPort) {
		return new ListDriversService(driverRepositoryPort);
	}

	@Bean
	UpdateDriverUseCase updateDriverUseCase(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		return new UpdateDriverService(driverRepositoryPort, clock);
	}

	@Bean
	DeleteDriverUseCase deleteDriverUseCase(DriverRepositoryPort driverRepositoryPort) {
		return new DeleteDriverService(driverRepositoryPort);
	}

	@Bean
	UpdateDriverLocationUseCase updateDriverLocationUseCase(
			DriverRepositoryPort driverRepositoryPort,
			Clock clock) {
		return new UpdateDriverLocationService(driverRepositoryPort, clock);
	}

	@Bean
	SetDriverOnlineUseCase setDriverOnlineUseCase(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		return new SetDriverOnlineService(driverRepositoryPort, clock);
	}

	@Bean
	SetDriverOfflineUseCase setDriverOfflineUseCase(DriverRepositoryPort driverRepositoryPort, Clock clock) {
		return new SetDriverOfflineService(driverRepositoryPort, clock);
	}
}
