package com.fleetbite.vehicle.application.service;

import com.fleetbite.shared.application.exception.ResourceNotFoundException;
import com.fleetbite.shared.domain.time.BusinessTime;
import com.fleetbite.vehicle.application.port.out.VehicleRepositoryPort;
import com.fleetbite.vehicle.domain.model.Vehicle;
import com.fleetbite.vehicle.domain.model.VehicleId;
import com.fleetbite.vehicle.domain.model.VehicleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAndListVehicleServiceTest {

	private static final OffsetDateTime CREATED =
			OffsetDateTime.of(2026, 8, 8, 22, 0, 0, 0, BusinessTime.ZONE_OFFSET);

	@Mock
	private VehicleRepositoryPort vehicleRepositoryPort;

	private GetVehicleByIdService getVehicleByIdService;
	private ListVehiclesService listVehiclesService;

	@BeforeEach
	void setUp() {
		getVehicleByIdService = new GetVehicleByIdService(vehicleRepositoryPort);
		listVehiclesService = new ListVehiclesService(vehicleRepositoryPort);
	}

	@Test
	void get_shouldReturnResult() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findById(vehicle.id())).thenReturn(Optional.of(vehicle));

		var result = getVehicleByIdService.execute(vehicle.id());

		assertEquals(vehicle.id().value(), result.id());
		assertEquals("ABC-123", result.plate());
	}

	@Test
	void get_shouldThrowNotFound() {
		VehicleId id = VehicleId.generate();
		when(vehicleRepositoryPort.findById(id)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> getVehicleByIdService.execute(id));
	}

	@Test
	void list_shouldMapAll() {
		Vehicle vehicle = Vehicle.create(VehicleId.generate(), "ABC-123", VehicleType.MOTORCYCLE, CREATED);
		when(vehicleRepositoryPort.findAll()).thenReturn(List.of(vehicle));

		var results = listVehiclesService.execute();

		assertEquals(1, results.size());
		assertEquals(vehicle.id().value(), results.getFirst().id());
	}
}
